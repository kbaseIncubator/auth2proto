package us.kbase.auth2.service.api;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.exceptions.AuthError;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.MissingParameterException;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingToken;

@Path("/api/legacy/KBase/Sessions/Login")
public class LegacyKBase {
	
	@Inject
	private Authentication auth;
	
	@GET
	public void dummyGetMethod() throws AuthenticationException {
		throw new AuthenticationException(AuthError.UNSUPPORTED_OP, 
				"This is just here for compatibility with the old client: " +
				"\"user_id\": null");
	}
	
	// this just exists to capture requests when the content-type header isn't
	// set. It seems to be chosen first repeatably. The method below will throw
	// an ugly error about the @FormParam otherwise.
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public void dummyErrorMethod() throws MissingParameterException {
		throw new MissingParameterException("token");
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> kbaseLogin(
			@FormParam("token") final String token,
			@FormParam("fields") String fields)
			throws AuthenticationException, AuthStorageException,
			MissingParameterException {
		if (token == null || token.isEmpty()) {
			throw new MissingParameterException("token");
		}
		if (fields == null) {
			fields = "";
		}
		//this is totally stupid.
		final String[] f = fields.split(",");
		final Map<String, Object> ret = new HashMap<>();
		boolean name = false;
		boolean email = false;
		for (int i = 0; i < f.length; i++) {
			final String field = f[i].trim();
			if ("name".equals(field)) {
				name = true;
			} else if ("email".equals(field)) {
				email = true;
			} else if ("token".equals(field)) {
				ret.put("token", token);
			}
		}

		final IncomingToken in = new IncomingToken(token);
		if (name || email) {
			final AuthUser u = auth.getUser(in);
			if (name) {
				ret.put("name", u.getFullName());
			}
			if (email) {
				ret.put("email", u.getEmail());
			}
			ret.put("user_id", u.getUserName().getName());
		} else {
			final HashedToken ht = auth.getToken(in);
			ret.put("user_id", ht.getUserName().getName());
		}
		return ret;
	}
	

}
