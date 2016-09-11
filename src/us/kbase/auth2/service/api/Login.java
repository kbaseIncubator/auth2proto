package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.CookieUtils.getLoginCookie;
import static us.kbase.auth2.service.api.CookieUtils.getMaxCookieAge;

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
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.LoginIdentities;
import us.kbase.auth2.lib.LoginToken;
import us.kbase.auth2.lib.UserName;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.ErrorType;
import us.kbase.auth2.lib.exceptions.InvalidTokenException;
import us.kbase.auth2.lib.exceptions.MissingParameterException;
import us.kbase.auth2.lib.exceptions.NoSuchIdentityProviderException;
import us.kbase.auth2.lib.exceptions.NoTokenProvidedException;
import us.kbase.auth2.lib.exceptions.UserExistsException;
import us.kbase.auth2.lib.identity.IdentityProvider;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.IncomingToken;
import us.kbase.auth2.lib.token.NewToken;
import us.kbase.auth2.lib.token.TemporaryToken;

@Path("/login")
public class Login {

	//TODO TEST
	//TODO JAVADOC
	//TODO NOW test entire api with nginx path changes (e.g. location /foo/bar mapped to / of this server). Issues - cookies, urls passed into forms. Not issues: redirects.
	//TODO NOW add last login date
	//TODO NOW add account created date
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
			return Response.seeOther(target).cookie(getStateCookie(state))
					.build();
		} else {
			final Map<String, Object> ret = new HashMap<>();
			final List<Map<String, String>> provs = new LinkedList<>();
			ret.put("providers", provs);
			for (final IdentityProvider idp: auth.getIdentityProviders()) {
				final Map<String, String> rep = new HashMap<>();
				rep.put("name", idp.getProviderName());
				final URI i = idp.getImageURI();
				rep.put("img", i.isAbsolute() ? i.toString() : ".." + i);
				provs.add(rep);
			}
			ret.put("hasprov", !provs.isEmpty());
			ret.put("urlpre", "?provider=");
			return Response.ok().entity(new Viewable("/loginstart", ret))
					.build();
		}
	}

	private NewCookie getStateCookie(final String state) {
		return getStateCookie(state, false);
	}
	
	private NewCookie getStateCookie(
			final String state,
			final boolean delete) {
		return new NewCookie(
				//TODO TEST path works with nginx path rewriting
				new Cookie("statevar", state, "/login/complete", null),
						"loginstate", delete ? 0 : 30 * 60, false);
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
		final LoginToken lr = auth.login(provider, authcode);
		final Response r;
		// always redirect so the authcode doesn't remain in the title bar
		// note nginx will rewrite the redirect appropriately so absolute
		// redirects are ok
		if (lr.isLoggedIn()) {
			//TODO NOW use provided redirect, default to user profile
			r = Response.seeOther(toURI("/tokens"))
			//TODO NOW can't set keep me logged in here, so set in profile
					.cookie(getLoginCookie(lr.getToken(), true))
					.cookie(getStateCookie("no state", true)).build();
		} else {
			r = Response.seeOther(toURI("/login/complete")).cookie(
					getLoginInProcessCookie(lr.getTemporaryToken()))
					.cookie(getStateCookie("no state", true))
					.build();
		}
		return r;
	}

	private NewCookie getLoginInProcessCookie(final TemporaryToken token) {
		return new NewCookie(new Cookie("in-process-login-token",
				token == null ? "no token" : token.getToken(),
						//TODO TEST cookies work with nginx path rewriting
				"/login", null),
				"authtoken", token == null ? 0 : getMaxCookieAge(token, false),
				false);
	}

	@GET
	@Path("/complete")
	@Template(name = "/loginchoice")
	public Map<String, Object> loginComplete(
			@CookieParam("in-process-login-token") final String token,
			@Context final UriInfo uriInfo)
			throws NoTokenProvidedException, AuthStorageException,
			InvalidTokenException {
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing in-process-login-token");
		}
		final LoginIdentities ids = auth.getLoginState(
				new IncomingToken(token.trim()));
		
		final Map<String, Object> ret = new HashMap<>();
		if (ids.getPrimaryUser() == null) {
			ret.put("create", true);
			ret.put("provider", ids.getPrimary().getProvider());
			ret.put("id", ids.getPrimary().getId());
			//TODO NOW get safe username from db
			ret.put("usernamesugg", ids.getPrimary().getUsername()
					.split("@")[0]);
			ret.put("username", ids.getPrimary().getUsername());
			ret.put("fullname", ids.getPrimary().getFullname());
			ret.put("email", ids.getPrimary().getEmail());
			final String createurl;
			if (uriInfo.getAbsolutePath().toString().endsWith("/")) {
				createurl = "../create";
			} else {
				createurl = "./create";
			}
			ret.put("createurl", createurl);
			//TODO NOW handle secondaries
			
		} else {
			//TODO NOW handle secondaries
			//TODO NOW handle primary with authuser
		}
		
		return ret;
	}
	
	@POST
	@Path("/create")
	public Response createUser(
			@CookieParam("in-process-login-token") final String token,
			@FormParam("provider") final String provider,
			@FormParam("id") final String remoteID,
			@FormParam("user") final String userName,
			@FormParam("full") final String fullName,
			@FormParam("email") final String email,
			@FormParam("stayLoggedIn") final String stayLoggedIn,
			@FormParam("private") final String nameAndEmailPrivate)
			throws AuthenticationException, AuthStorageException,
				UserExistsException, NoTokenProvidedException {
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing in-process-login-token");
		}
		//TODO NOW sanity check inputs
		final boolean sessionLogin = stayLoggedIn == null ||
				stayLoggedIn.isEmpty();
		final boolean priv = nameAndEmailPrivate != null &&
				nameAndEmailPrivate.isEmpty();
		
		
		// might want to enapsulate the user data in a NewUser class
		final NewToken newtoken = auth.createUser(new IncomingToken(token),
				provider, remoteID, new UserName(userName), fullName, email,
				sessionLogin, priv);
		//TODO NOW use provided redirect, default to user profile
		return Response.seeOther(toURI("/tokens"))
		//TODO NOW can't set keep me logged in here, so set in profile
				.cookie(getLoginCookie(newtoken, true))
				.cookie(getLoginInProcessCookie(null)).build();
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
