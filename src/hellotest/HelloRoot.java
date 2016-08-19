package hellotest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class HelloRoot {
	
	@Inject
	private HelloApplicationResources res;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public HelloJson sayHello() {
		System.out.println("hello root");
		return new HelloJson("Hello root!", "res: " + res);
	}
}
