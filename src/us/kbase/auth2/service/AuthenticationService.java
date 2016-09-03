package us.kbase.auth2.service;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.StorageInitException;
import us.kbase.auth2.lib.storage.mongo.MongoStorage;
import us.kbase.auth2.service.LoggingFilter;
import us.kbase.auth2.service.exceptions.AuthConfigurationException;
import us.kbase.auth2.service.exceptions.ExceptionHandler;
import us.kbase.auth2.service.kbase.KBaseAuthConfig;
import us.kbase.auth2.service.template.TemplateProcessor;
import us.kbase.auth2.service.template.mustache.MustacheProcessor;

//TODO WAIT accept json in text/plain and application/x-www-form-urlencoded or manually handle it

public class AuthenticationService extends ResourceConfig {
	
	//TODO TEST
	//TODO JAVADOC
	
	private static final String ID_PROV_GOOGLE = "google";
	private static final String ID_PROV_GLOBUS = "globus";
	
	
	private static MongoClient mc;
	@SuppressWarnings("unused")
	private final SLF4JAutoLogger logger; //keep a reference to prevent GC
	
	public AuthenticationService()
			throws StorageInitException, AuthConfigurationException {
		quietLogger();
		final AuthConfig c = new KBaseAuthConfig();
		logger = c.getLogger();
		try {
			buildApp(c);
		} catch (StorageInitException e) {
			LoggerFactory.getLogger(getClass()).error(
					"Failed to initialize storage engine: " + e.getMessage(),
					e);
			throw e;
		} catch (AuthConfigurationException e) {
			LoggerFactory.getLogger(getClass()).error(
					"Invalid configuration: " + e.getMessage(), e);
			throw e;
		}
	}

	private void quietLogger() {
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
				.setLevel(Level.INFO);
	}

	private void buildApp(final AuthConfig c)
			throws StorageInitException, AuthConfigurationException {
		synchronized(this) {
			if (mc == null) {
				mc = buildMongo(c);
			}
		}
		packages("us.kbase.auth2.service.api");
		register(JacksonFeature.class);
		register(MustacheMvcFeature.class);
		property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "templates");
		register(LoggingFilter.class);
		register(ExceptionHandler.class);
		final Authentication auth = buildAuth(c, mc);
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(auth).to(Authentication.class);
				bind(new MustacheProcessor(Paths.get("templates")
						.toAbsolutePath()))
					.to(TemplateProcessor.class);
			}
		});
	}
	
	private MongoClient buildMongo(final AuthConfig c) {
		//TODO ZLATER handle shards
		try {
			return new MongoClient(c.getMongoHost());
		} catch (MongoException e) {
			LoggerFactory.getLogger(getClass()).error(
					"Failed to connect to MongoDB: " + e.getMessage(), e);
			throw e;
		}
	}
	
	private Authentication buildAuth(final AuthConfig c, final MongoClient mc)
			throws StorageInitException, AuthConfigurationException {
		final MongoDatabase db;
		try {
			db = mc.getDatabase(c.getMongoDatabase());
		} catch (MongoException e) {
			LoggerFactory.getLogger(getClass()).error(
					"Failed to get database from MongoDB: " + e.getMessage(),
					e);
			throw e;
		}
		//TODO MONGO & TEST authenticate to db with user/pwd
		final AuthStorage s = new MongoStorage(db);
		return new Authentication(s, getIdentityProviders(c));
	}
	
	private Set<IdentityProvider> getIdentityProviders(final AuthConfig c)
			throws AuthConfigurationException {
		final Set<IdentityProvider> ips = new HashSet<>();
		for (final IdentityProviderConfig idc:
				c.getIdentityProviderConfigs()) {
			switch (idc.getIdentityProviderName()) {
				case ID_PROV_GOOGLE:
					ips.add(new GoogleIdentityProvider(idc));
					break;
				case ID_PROV_GLOBUS:
					ips.add(new GlobusIdentityProvider(idc));
					break;
				default:
					throw new AuthConfigurationException(
							"Unknown identity provider: " +
							idc.getIdentityProviderName());
			}
				
		}
		return ips;
	}

	static void shutdown() {
		mc.close();
	}
}
