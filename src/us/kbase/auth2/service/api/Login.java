package us.kbase.auth2.service.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Viewable;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.exceptions.NoSuchIdentityProviderException;
import us.kbase.auth2.lib.identity.IdentityProvider;

@Path("/login")
public class Login {

	//TODO TEST
	//TODO JAVADOC
	
	@Inject
	private Authentication auth;
	
	@GET
	public Response loginStart(
			@QueryParam("provider") final String provider)
					throws NoSuchIdentityProviderException {
		//TODO CONFIG allow enable & disable of id providers.
		//TODO NOW redirect url
		if (provider != null && !provider.trim().isEmpty()) {
			final IdentityProvider idp = auth.getIdentityProvider(
					provider);
			final String state = auth.getBareToken();
			final URI target = toURI(idp.getLoginURL(state));
			return Response.temporaryRedirect(target).cookie(new NewCookie(
					new Cookie("statevar", state, "/login", null),
							"loginstate", 30 * 60, false)).build();
			//TODO NOW make secure cookie configurable
		} else {
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
			return Response.ok().entity(new Viewable("/loginstart", ret))
					.build();
		}
	}
	
	//Assumes valid URI in URL form
	private URI toURI(final URL loginURL) {
		try {
			return loginURL.toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException("This should be impossible", e);
		}
	}
}
