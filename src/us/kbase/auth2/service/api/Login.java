package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.APIUtils.relativize;
import static us.kbase.auth2.service.api.APIUtils.getLoginCookie;
import static us.kbase.auth2.service.api.APIUtils.getMaxCookieAge;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import us.kbase.auth2.lib.AuthUser;
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
import us.kbase.auth2.lib.identity.RemoteIdentity;
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
			@QueryParam("provider") final String provider,
			@Context UriInfo uriInfo)
					throws NoSuchIdentityProviderException {
		//TODO CONFIG allow enable & disable of id providers.
		//TODO NOW redirect url
		if (provider != null && !provider.trim().isEmpty()) {
			final IdentityProvider idp = auth.getIdentityProvider(
					provider);
			final String state = auth.getBareToken();
			final URI target = toURI(idp.getLoginURL(state, false));
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
				if (i.isAbsolute()) {
					rep.put("img", i.toString());
				} else {
					rep.put("img", relativize(uriInfo, i));
				}
				provs.add(rep);
			}
			ret.put("hasprov", !provs.isEmpty());
			ret.put("urlpre", "?provider=");
			return Response.ok().entity(new Viewable("/loginstart", ret))
					.build();
		}
	}

	private NewCookie getStateCookie(final String state) {
		return new NewCookie(new Cookie(
				"statevar", state == null ? "no state" : state,
						"/login/complete", null),
				"loginstate", state == null ? 0 : 30 * 60,
						APIConstants.SECURE_COOKIES);
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
		//TODO NOW handle returned OAuth error code in queryparams
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
					.cookie(getStateCookie(null)).build();
		} else {
			r = Response.seeOther(toURI("/login/complete")).cookie(
					getLoginInProcessCookie(lr.getTemporaryToken()))
					.cookie(getStateCookie(null))
					.build();
		}
		return r;
	}

	private NewCookie getLoginInProcessCookie(final TemporaryToken token) {
		return new NewCookie(new Cookie("in-process-login-token",
				token == null ? "no token" : token.getToken(), "/login", null),
				"authtoken", token == null ? 0 : getMaxCookieAge(token, false),
				APIConstants.SECURE_COOKIES);
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
		ret.put("provider", ids.getPrimary().getProvider());
		final List<Map<String, String>> secs = new LinkedList<>();
		ret.put("secs", secs);
		for (final Entry<RemoteIdentity, AuthUser> e:
				ids.getSecondaries().entrySet()) {
			final Map<String, String> s = new HashMap<>();
			s.put("prov_id", e.getKey().getId());
			s.put("prov_username", e.getKey().getUsername());
			s.put("username", e.getValue().getUserName().getName());
			secs.add(s);
		}
		if (ids.getPrimaryUser() == null) {
			ret.put("create", true);
			ret.put("prov_id", ids.getPrimary().getId());
			//TODO NOW get safe username from db
			ret.put("usernamesugg", ids.getPrimary().getUsername()
					.split("@")[0]);
			ret.put("prov_username", ids.getPrimary().getUsername());
			ret.put("prov_fullname", ids.getPrimary().getFullname());
			ret.put("prov_email", ids.getPrimary().getEmail());
			ret.put("createurl",
					relativize(uriInfo, "/login/create"));
			
		} else {
			// if here we know there's at least one secondary, otherwise
			// the user would've been logged in at /complete/{provider}
			// possibility of a race condition, but worst case is the user has
			// to click the primary user with no other choices, so meh
			final Map<String, String> p = new HashMap<>();
			p.put("prov_id", ids.getPrimary().getId());
			p.put("prov_username", ids.getPrimary().getUsername());
			p.put("username", ids.getPrimaryUser().getUserName().getName());
			secs.add(p);
		}
		if (!secs.isEmpty()) {
			ret.put("hassecs", true);
			ret.put("pickurl", relativize(uriInfo, "/login/pick"));
		}
		return ret;
	}
	
	@POST
	@Path("/pick")
	public Response pickAccount(
			@CookieParam("in-process-login-token") final String token,
			@FormParam("provider") final String provider,
			@FormParam("id") final String remoteID)
			throws NoTokenProvidedException, AuthenticationException,
			AuthStorageException {
		
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing in-process-login-token");
		}
		final NewToken newtoken = auth.login(
				new IncomingToken(token), provider, remoteID);
		//TODO NOW use provided redirect, default to user profile
		return Response.seeOther(toURI("/tokens"))
				//TODO NOW can't set keep me logged in here, so set in profile
				.cookie(getLoginCookie(newtoken, true))
				.cookie(getLoginInProcessCookie(null)).build();
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
