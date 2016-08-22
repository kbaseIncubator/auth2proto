package us.kbase.auth2.service;

import java.nio.file.Paths;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.mongo.MongoStorage;
import us.kbase.auth2.service.LoggingFilter;
import us.kbase.auth2.service.exceptions.handler.ExceptionHandler;
import us.kbase.auth2.service.template.TemplateProcessor;
import us.kbase.auth2.service.template.mustache.MustacheProcessor;
import us.kbase.common.service.JsonServerSyslog;

//TODO WAIT accept json in text/plain and application/x-www-form-urlencoded or manually handle it

public class AuthenticationService extends ResourceConfig {
	
	private static MongoClient mc;
	@SuppressWarnings("unused")
	private final JsonServerSyslog logger; //keep a reference to prevent GC
	
	public AuthenticationService() {
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
			.setLevel(Level.INFO);
		//TODO AUTH allow setting name
		logger = new JsonServerSyslog("KBaseAuth2",
				"thisisafakekeythatshouldntexistihope",
				JsonServerSyslog.LOG_LEVEL_INFO, true);
		mc = buildMongo();
		packages("us.kbase.auth2.service.api");
		register(JacksonFeature.class);
		register(MustacheMvcFeature.class);
		property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "templates");
		register(LoggingFilter.class);
		register(ExceptionHandler.class);
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(buildAuth()).to(Authentication.class);
				bind(new MustacheProcessor(Paths.get("templates")
						.toAbsolutePath()))
					.to(TemplateProcessor.class);
			}
		});
	}
	
	private MongoClient buildMongo() {
		//TODO CONFIG make mongo loc & db configurable
		return new MongoClient("localhost:27017");
	}
	
	private Authentication buildAuth() {
		//TODO CONFIG make a configuration parsing class
		//TODO CONFIG make a builder class that takes a config and returns an Authentication
		final AuthStorage s = new MongoStorage(mc.getDatabase("kbaseauth"));
		return new Authentication(s);
	}
	
	static void shutdown() {
		mc.close();
	}
}
