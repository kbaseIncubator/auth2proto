package hellotest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

//TODO accept json in text/plain and application/x-www-form-urlencoded
//TODO inject a dependency
//TODO start a server programatically
//TODO logging

public class HelloApp extends ResourceConfig {
	public HelloApp() {
		packages("hellotest");
		register(JacksonFeature.class);
	}
}
