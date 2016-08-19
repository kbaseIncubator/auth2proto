package hellotest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Template;

@Path("/hellotemplate")
public class HelloTemplate {
	
	@Inject
	private HelloApplicationResources res;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Template(name = "/hello")
	public HelloJson sayHello() {
		System.out.println("hellotemplate");
		return new HelloJson("Hello there!", "res: " + res);
	}
}
