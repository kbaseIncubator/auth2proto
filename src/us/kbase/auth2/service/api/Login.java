package us.kbase.auth2.service.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.identity.IdentityProvider;

@Path("/login")
public class Login {

	//TODO TEST
	//TODO JAVADOC
	
	@Inject
	private Authentication auth;
	
	@GET
	public Response loginStart(
			@QueryParam("provider") final String provider) {
		//TODO NOW if provider isn't null, redirect
		//TODO CONFIG allow enable & disable of id providers.
		//TODO NOW redirect url
		final Map<String, Object> ret = new HashMap<>();
		final List<Map<String, String>> provs = new LinkedList<>();
		ret.put("providers", provs);
		for (final IdentityProvider idp: auth.getIdentityProviders()) {
			final Map<String, String> rep = new HashMap<>();
			rep.put("name", idp.getProviderName());
			rep.put("img", idp.getRelativeImageURL());
			provs.add(rep);
		}
		ret.put("hasprov", !provs.isEmpty());
		ret.put("urlpre", "/login?provider=");
		return Response.ok().entity(new Viewable("/loginstart", ret)).build();
	}
}
