package us.kbase.auth2;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import us.kbase.auth2.hello.Hello;

public class HelloApp2 extends Application {
	
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> cl = new HashSet<>();
		cl.add(Hello.class);
		return cl;
	}
	
}
