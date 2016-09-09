package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.CookieUtils.getCookie;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.mvc.Viewable;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.LoginResult;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.ErrorType;
import us.kbase.auth2.lib.exceptions.MissingParameterException;
import us.kbase.auth2.lib.exceptions.NoSuchIdentityProviderException;
import us.kbase.auth2.lib.exceptions.NoTokenProvidedException;
import us.kbase.auth2.lib.identity.IdentityProvider;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;

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
					new Cookie("statevar", state),
							"loginstate", 30 * 60, false)).build();
			//TODO NOW make secure cookie configurable
		} else {
			final Map<String, Object> ret = new HashMap<>();
			final List<Map<String, String>> provs = new LinkedList<>();
			ret.put("providers", provs);
			for (final IdentityProvider idp: auth.getIdentityProviders()) {
				final Map<String, String> rep = new HashMap<>();
				rep.put("name", idp.getProviderName());
				rep.put("img", ".." + idp.getRelativeImageURL());
				provs.add(rep);
			}
			ret.put("hasprov", !provs.isEmpty());
			ret.put("urlpre", "?provider=");
			return Response.ok().entity(new Viewable("/loginstart", ret))
					.build();
		}
	}
	
	@GET
	@Path("/complete/{provider}")
	public Response login(
			@PathParam("provider") String provider,
			@CookieParam("statevar") final String state,
			@Context final UriInfo uriInfo)
			throws MissingParameterException, AuthenticationException,
			NoSuchProviderException, AuthStorageException {
		//TODO NOW handle error in params (provider, state)
		provider = upperCase(provider);
		final MultivaluedMap<String, String> qps =
				uriInfo.getQueryParameters();
		final IdentityProvider idp = auth.getIdentityProvider(provider);
		final String authcode = qps.getFirst(idp.getAuthCodeQueryParamName());
		final String retstate = qps.getFirst("state"); //may need to be configurable
		if (state == null || state.trim().isEmpty()) {
			throw new MissingParameterException(
					"Couldn't retrieve state value from cookie");
		}
		if (!state.equals(retstate)) {
			throw new AuthenticationException(ErrorType.AUTHENTICATION_FAILED,
					"State values do not match, this may be a CXRF attack");
		}
		final LoginResult lr = auth.login(provider, authcode);
		final Response r;
		// always redirect so the authcode doesn't remain in the title bar
		// note nginx will rewrite the redirect appropriately so absolute
		// redirects are ok
		if (lr.isLoggedIn()) {
			//TODO NOW use provided redirect, default to user profile
			r = Response.temporaryRedirect(toURI("/tokens"))
			//TODO NOW can't set keep me logged in here, so set in profile
					.cookie(getCookie(lr.getToken(), true)).build();
		} else {
			r = Response.temporaryRedirect(toURI("/login/complete")).cookie(
					new NewCookie(new Cookie(
									"in-process-login-token",
									lr.getTemporaryToken().getToken(),
									"..", null),
							//TODO CONFIG make secure cookie configurable
							"authtoken", NewCookie.DEFAULT_MAX_AGE, false))
					.build();
			//TODO NOW make image paths URIs
		}
		return r;
	}
	
	@GET
	@Path("/complete")
	public Response loginComplete(
			@CookieParam("in-process-login-token") final String token)
			throws NoTokenProvidedException {
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing in-process-login-token");
		}
		System.out.println(token);
		//TODO NOW fetch ids from DB, show page for login/signup.
		return Response.ok().build();
	}
	
	// assumes non-null, len > 0
	private String upperCase(final String provider) {
		final String first = new String(Character.toChars(
				Character.toUpperCase(provider.codePointAt(0))));
		if (provider.length() == first.length()) {
			return first;
		}
		return first + provider.substring(first.length());
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
