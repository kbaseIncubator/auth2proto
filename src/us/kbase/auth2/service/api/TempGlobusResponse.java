package us.kbase.auth2.service.api;

import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("oauth2/redirect_handler")
public class TempGlobusResponse {

	// temporary until new creds are set up
	@GET
	public Response handleGlobusTemp(
			@QueryParam("state") final String state,
			@QueryParam("code") final String authCode,
			@CookieParam("temp-link") final String link) {
		final String path;
		if ("true".equals(link)) {
			path = "/link/complete/globus";
		} else {
			path = "/login/complete/globus";
		}
		return Response.temporaryRedirect(
				UriBuilder.fromPath(path)
					.queryParam("state", state)
					.queryParam("code", authCode)
					.build())
				.cookie(new NewCookie(new Cookie("temp-link", "false", "/", null), "temporary cookie", 0, false))
				.build();
	}
}
