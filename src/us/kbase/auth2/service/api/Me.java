package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.APIUtils.relativize;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.mvc.Template;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.exceptions.InvalidTokenException;
import us.kbase.auth2.lib.exceptions.NoTokenProvidedException;
import us.kbase.auth2.lib.identity.RemoteIdentity;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.IncomingToken;

@Path("/me")
public class Me {

	//TODO TEST
	//TODO JAVADOC
	//TODO NOW don't expose provider IDs. Make own id for the remote id. Check entire UI for this.
	
	@Inject
	private Authentication auth;
	
	@GET
	@Template(name = "/me")
	public Map<String, Object> me(
			@CookieParam("token") final String token,
			@Context final UriInfo uriInfo)
			throws NoTokenProvidedException, InvalidTokenException,
			AuthStorageException {
		//TODO NOW handle keep logged in, private
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException();
		}
		final AuthUser u = auth.getUser(new IncomingToken(token));
		final Map<String, Object> ret = new HashMap<>();
		ret.put("userupdateurl", relativize(uriInfo, "/me"));
		ret.put("unlinkprefixurl", relativize(uriInfo, "/me/"));
		ret.put("user", u.getUserName().getName());
		ret.put("local", u.isLocal());
		ret.put("fullname", u.getFullName());
		ret.put("email", u.getEmail());
		ret.put("customroles", u.getCustomRoles());
		ret.put("unlink", u.getIdentities().size() > 1);
		ret.put("roles", u.getRoles().stream().map(r -> r.getRole())
				.collect(Collectors.toList()));
		final List<Map<String, String>> idents = new LinkedList<>();
		ret.put("idents", idents);
		for (final RemoteIdentity ri: u.getIdentities()) {
			final Map<String, String> i = new HashMap<>();
			i.put("provider", ri.getProvider());
			i.put("username", ri.getUsername());
			i.put("id", ri.getId());
			idents.add(i);
		}
		return ret;
	}
}
