package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.APIUtils.relativize;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.mvc.Viewable;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.exceptions.InvalidTokenException;
import us.kbase.auth2.lib.exceptions.NoSuchIdentityProviderException;
import us.kbase.auth2.lib.exceptions.NoTokenProvidedException;
import us.kbase.auth2.lib.identity.IdentityProvider;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.IncomingToken;

@Path("/link")
public class Link {

	@Inject
	private Authentication auth;
	
	@GET
	public Response linkStart(
			@CookieParam("token") final String token,
			@QueryParam("provider") final String provider,
			@Context UriInfo uriInfo)
			throws NoSuchIdentityProviderException, NoTokenProvidedException,
			InvalidTokenException, AuthStorageException {
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException();
		}
		
		//TODO CONFIG allow enable & disable of id providers.
		if (provider != null && !provider.trim().isEmpty()) {
			final IdentityProvider idp = auth.getIdentityProvider(
					provider);
			final String state = auth.getBareToken();
			final URI target = toURI(idp.getLoginURL(state, true));
			return Response.seeOther(target).cookie(getStateCookie(state))
					.build();
		} else {
			final AuthUser u = auth.getUser(new IncomingToken(token));
			final Map<String, Object> ret = new HashMap<>();
			ret.put("user", u.getUserName().getName());
			final List<Map<String, String>> provs = new LinkedList<>();
			ret.put("providers", provs);
			for (final IdentityProvider idp: auth.getIdentityProviders()) {
				final Map<String, String> rep = new HashMap<>();
				rep.put("name", idp.getProviderName());
				final URI i = idp.getImageURI();
				if (i.isAbsolute()) {
					rep.put("img", i.toString());
				} else {
					rep.put("img", relativize(uriInfo, i));
				}
				provs.add(rep);
			}
			ret.put("hasprov", !provs.isEmpty());
			ret.put("urlpre", "?provider=");
			return Response.ok().entity(new Viewable("/linkstart", ret))
					.build();
		}
	}
	
	private NewCookie getStateCookie(final String state) {
		return new NewCookie(new Cookie(
				"statevar", state == null ? "no state" : state,
						"/link/complete", null),
				"linkstate", state == null ? 0 : 30 * 60,
						APIConstants.SECURE_COOKIES);
	}
	
	//Assumes valid URI in URL form
	private URI toURI(final URL loginURL) {
		try {
			return loginURL.toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException("This should be impossible", e);
		}
	}
	
	//Assumes valid URI in String form
	private URI toURI(final String uri) {
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException("This should be impossible", e);
		}
	}
}
