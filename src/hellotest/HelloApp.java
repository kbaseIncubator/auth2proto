package hellotest;

import java.nio.file.Paths;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.mustache.MustacheMvcFeature;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import us.kbase.auth2.service.LoggingFilter;
import us.kbase.auth2.service.template.mustache.MustacheProcessor;
import us.kbase.auth2.service.ExceptionHandler;

//TODO WAIT accept json in text/plain and application/x-www-form-urlencoded or manually handle it

public class HelloApp extends ResourceConfig {
	
	public HelloApp() {
		((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
			.setLevel(Level.INFO);
		packages("hellotest");
		register(JacksonFeature.class);
		register(MustacheMvcFeature.class);
		property(MustacheMvcFeature.TEMPLATE_BASE_PATH, "templates");
		register(LoggingFilter.class);
		register(ExceptionHandler.class);
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(new HelloApplicationResources())
					.to(HelloApplicationResources.class);
				bind(new MustacheProcessor(Paths.get("templates")
						.toAbsolutePath()))
					.to(MustacheProcessor.class);
			}
		});
	}
}
