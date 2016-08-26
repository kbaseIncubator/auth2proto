package us.kbase.auth2.service.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Template;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.exceptions.AuthError;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingToken;
import us.kbase.auth2.lib.token.TokenSet;

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
			@CookieParam("token") final String token)
			throws AuthenticationException, AuthStorageException {
		final Map<String, Object> t = getTokens(token);
		t.put("user", ((APIToken) t.get("current")).getUser());
		t.put("targeturl", "/tokens/create");
		return t;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> getTokensJSON(
			@CookieParam("token") final String cookieToken,
			@HeaderParam("authentication") final String headerToken)
			throws AuthenticationException, AuthStorageException {
		return getTokens(cookieToken == null ? headerToken : cookieToken);
	}
	
	@POST
	@Path("/create")
	@Produces(MediaType.TEXT_HTML)
	@Template(name = "/createtoken")
	public APINewToken createTokenHTML(
			@CookieParam("token") final String userToken,
			@FormParam("tokenname") final String tokenName,
			@FormParam("tokentype") final String tokenType)
			throws AuthException, AuthStorageException {
		return createtoken(tokenName, tokenType, userToken);
	}
	
	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public APINewToken createTokenJSON(
			@CookieParam("token") final String cookieToken,
			@HeaderParam("authentication") final String headerToken,
			final CreateTokenParams input)
			throws AuthException, AuthStorageException {
		return createtoken(input.getName(), input.getType(),
				cookieToken == null || cookieToken.isEmpty() ?
						headerToken : cookieToken);
	}

	private APINewToken createtoken(
			final String tokenName,
			final String tokenType,
			final String userToken)
			throws AuthenticationException, AuthException,
			AuthStorageException {
		checkToken(userToken);
		return new APINewToken(auth.createToken(new IncomingToken(userToken),
				tokenName, tokenType == "server"));
	}

	private void checkToken(final String token)
			throws AuthenticationException {
		if (token == null) {
			throw new AuthenticationException(AuthError.NO_TOKEN, 
					"An authentication token must be supplied in the request.");
		}
	}
	
	private Map<String, Object> getTokens(final String token)
			throws AuthenticationException, AuthStorageException {
		checkToken(token);
		final TokenSet ts = auth.getTokens(new IncomingToken(token));
		final Map<String, Object> ret = new HashMap<>();
		ret.put("current", new APIToken(ts.getCurrentToken()));
		
		// should try out streams here
		final List<APIToken> ats = new LinkedList<>();
		for (final HashedToken t: ts.getTokens()) {
			ats.add(new APIToken(t));
		}
		ret.put("tokens", ats);
		return ret;
	}
	

}
