package us.kbase.auth2.service.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("oauth2/redirect_handler")
public class TempGlobusResponse {

	// temporary until new creds are set up
	@GET
	public Response handleGlobusTemp(
			@QueryParam("state") final String state,
			@QueryParam("code") final String authCode) {
		return Response.temporaryRedirect(
				UriBuilder.fromPath("/login/complete/globus")
					.queryParam("state", state)
					.queryParam("code", authCode)
					.build())
				.build();
	}
}
