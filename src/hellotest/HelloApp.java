package hellotest;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

//TODO WAIT accept json in text/plain and application/x-www-form-urlencoded or manually handle it
//TODO NOW logging

public class HelloApp extends ResourceConfig {
	public HelloApp() {
		packages("hellotest");
		register(JacksonFeature.class);
		register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(new HelloApplicationResources())
					.to(HelloApplicationResources.class);
			}
		});
	}
}
