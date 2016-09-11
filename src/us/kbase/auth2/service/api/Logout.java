package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.CookieUtils.getLogoutCookie;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import com.google.common.collect.ImmutableMap;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.exceptions.InvalidTokenException;
import us.kbase.auth2.lib.exceptions.NoTokenProvidedException;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingToken;

@Path("/logout")
public class Logout {

	@Inject
	private Authentication auth;
	
	@GET
	@Template(name = "/logout")
	public Map<String, String> logout(
			@CookieParam("token") final String token)
			throws AuthStorageException, NoTokenProvidedException,
			InvalidTokenException {
		checkToken(token);
		final HashedToken ht = auth.getToken(new IncomingToken(token));
		return ImmutableMap.of("user", ht.getUserName().getName(),
				"logouturl", "/logout/result");
	}
	
	@POST
	@Path("/result")
	public Response logoutResult(
			@CookieParam("token") final String token)
			throws AuthStorageException, NoTokenProvidedException {
		checkToken(token);
		final HashedToken ht = auth.revokeToken(new IncomingToken(token));
		return Response.ok(
				new Viewable("/logoutresult",
						ImmutableMap.of("user", ht == null ? null :
							ht.getUserName().getName())))
				.cookie(getLogoutCookie())
				.build();
	}
	
	//TODO NOW make this a convenience method - API helper class
	private void checkToken(final String token)
			throws NoTokenProvidedException {
		if (token == null || token.isEmpty()) {
			throw new NoTokenProvidedException(
					"An authentication token must be supplied in the request.");
		}
	}
	
}
