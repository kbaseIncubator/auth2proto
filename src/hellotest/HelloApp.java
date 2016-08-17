package hellotest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class HelloApp extends ResourceConfig {
	public HelloApp() {
		packages("us.kbase.auth2");
		register(JacksonFeature.class);
	}
}
