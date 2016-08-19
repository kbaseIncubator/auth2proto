package hellotest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


// Plain old Java Object it does not extend as class or implements 
// an interface

// The class registers its methods for the HTTP GET request using the @GET annotation. 
// Using the @Produces annotation, it defines that it can deliver several MIME types,
// text, XML and HTML. 

// The browser requests per default the HTML MIME type.

//Sets the path to base URL + /hello
@Path("/hello")
public class Hello {
	
	@Inject
	private HelloApplicationResources res;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public HelloJson sayHello() {
		return new HelloJson("Hello there!", "res: " + res);
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN}) // text doesn't work
	@Produces(MediaType.APPLICATION_JSON)
	public HelloJson sayHelloPost(HelloJson h) {
		return new HelloJson(h.getBar() + "p", h.getWhee() + "p");
	}
	
	@Path("/error")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response sayHelloError(@QueryParam("exp") String exp) {
		if ("args".equals(exp)) {
			throw new IllegalArgumentException("args");
		} else if ("web".equals(exp)) {
			throw new WebApplicationException(Response.status(502).build());
		}
		
		return Response.status(401).entity(
				new HelloError(401, "unauthorized")).build();
		
	}
	
	@Path("/error")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response sayHelloErrorHTML(@QueryParam("exp") String exp) {
		if ("args".equals(exp)) {
			throw new IllegalArgumentException("args");
		} else if ("web".equals(exp)) {
			throw new WebApplicationException(Response.status(502).build());
		}
		
		return Response.status(401).entity(
				new HelloError(401, "unauthorized")).build();
		
	}
} 
