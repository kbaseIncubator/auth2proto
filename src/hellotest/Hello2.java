package hellotest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello2")
public class Hello2 {
	
	@Inject
	private HelloApplicationResources res;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public HelloJson sayHello() {
		return new HelloJson("Hello there!", "res: " + res);
	}
}
