package us.kbase.auth2.service.api;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.exceptions.AuthError;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.UnauthorizedException;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingToken;

@Path("/api/legacy/globus")
public class LegacyGlobus {

	//TODO TEST
	//TODO JAVADOC
	
	@Inject
	private Authentication auth;
	
	// note that access_token_hash is not returned in the structure
	// also note that unlike the globus api, this does not refresh the token
	// also note that the error structure is completely different. 
	@GET
	@Path("/token")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> introspectToken(
			@HeaderParam("x-globus-goauthtoken") final String xtoken,
			@HeaderParam("globus-goauthtoken") String token,
			@QueryParam("grant_type") final String grantType)
			throws AuthException, AuthStorageException {

		if (!"client_credentials".equals(grantType)) {
			throw new AuthException(AuthError.UNSUPPORTED_OP,
					"Only client_credentials grant_type supported. Got " +
					grantType);
		}
		if (token == null || token.isEmpty()) {
			token = xtoken;
			if (token == null || token.isEmpty()) {
				// globus throws a 403 instead of a 401
				throw new UnauthorizedException(AuthError.NO_TOKEN, "");
			}
		}
		final HashedToken ht;
		try {
			ht = auth.getToken(new IncomingToken(token));
		} catch (AuthenticationException e) {
			// globus throws a 403 instead of a 401
			throw new UnauthorizedException(
					e.getErr(), "Authentication failed.");
		}
		final long created = (long) Math.floor(
				ht.getCreationDate().getTime() / 1000.0);
		final long expires = (long) Math.floor(
				ht.getExpirationDate().getTime() / 1000.0);
		final Map<String, Object> ret = new HashMap<>();
		ret.put("access_token", token);
		ret.put("client_id", ht.getUserName().getName());
		ret.put("expires_in", expires - new Date().getTime());
		ret.put("expiry", expires);
		ret.put("issued_on", created);
		ret.put("lifetime", expires - created);
		ret.put("refresh_token", "");
		ret.put("scopes", new LinkedList<String>());
		ret.put("token_id", ht.getId().toString());
		ret.put("token_type", "Bearer");
		ret.put("user_name", ht.getUserName().getName());
		return ret;
	}
}
