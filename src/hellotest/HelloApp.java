package hellotest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

//TODO accept json automatically
//TODO return a json error
//TODO inject a dependency
//TODO start a server programatically
//TODO stop the server
//TODO logging

public class HelloApp extends ResourceConfig {
	public HelloApp() {
		packages("hellotest");
		register(JacksonFeature.class);
	}
}
