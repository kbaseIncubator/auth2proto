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

import us.kbase.auth2.lib.exceptions.AuthError;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.UnauthorizedException;


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
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
	public Response sayHelloError(@QueryParam("exp") final String exp)
			throws AuthException {
		if (exp == null) {
			throw new WebApplicationException(Response.status(401).build());
		}
		switch (exp) {
			case "args":
				throw new IllegalArgumentException("args");
			case "web":
				throw new WebApplicationException(Response.status(502)
						.build());
			case "badreq":
				throw new AuthException(AuthError.MISSING_PARAMETER, "badreq",
						new IllegalArgumentException("badreq"));
			case "auth":
				throw new AuthenticationException(AuthError.AUTHENTICATION_FAILED,
						"auth", new IllegalArgumentException("auth"));
			case "unauth":
				throw new UnauthorizedException(AuthError.UNAUTHORIZED, "unauth",
						new IllegalArgumentException("unauth"));
		}
		throw new IllegalArgumentException("foo");
	}
}
