package us.kbase.auth2;

import org.glassfish.jersey.server.ResourceConfig;

public class HelloApp extends ResourceConfig {
	public HelloApp() {
		packages("us.kbase.auth2");
	}
}
