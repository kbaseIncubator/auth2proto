package us.kbase.auth2.service.api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Template;

import com.google.common.collect.ImmutableMap;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.HashedToken;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.IncomingToken;
import us.kbase.auth2.service.exceptions.AuthError;
import us.kbase.auth2.service.exceptions.AuthenticationException;

@Path("/tokens")
public class Tokens {
	
	//TODO TEST
	//TODO JAVADOC

	@Inject
	private Authentication auth;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Template(name = "/tokens")
	public Map<String, Object> getTokensHTML(
			@CookieParam("token") String token)
			throws AuthenticationException, AuthStorageException {
		final List<APIToken> t = getTokens(token);
		return ImmutableMap.of("user", t.get(0).getUserName(),
				"tokens", t);
	}
	
	//TODO NOW test when token creation works
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<APIToken> getTokensJSON(
			@CookieParam("token") final String cookieToken,
			@HeaderParam("authentication") final String headerToken)
			throws AuthenticationException, AuthStorageException {
		return getTokens(cookieToken == null ? headerToken : cookieToken);
	}
	
	private List<APIToken> getTokens(final String token)
			throws AuthenticationException, AuthStorageException {
		if (token == null) {
			throw new AuthenticationException(AuthError.NO_TOKEN, 
					"An authentication token must be supplied in the request.");
		}
		// should try out streams here
		final List<APIToken> ret = new LinkedList<>();
		for (final HashedToken t: auth.getTokens(new IncomingToken(token))) {
			ret.add(new APIToken(t));
		}
		return ret;
	}
	

}
