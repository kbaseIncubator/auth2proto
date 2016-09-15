package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.APIUtils.relativize;
import static us.kbase.auth2.service.api.APIUtils.getLoginCookie;
import static us.kbase.auth2.service.api.APIUtils.getMaxCookieAge;
import static us.kbase.auth2.service.api.APIUtils.upperCase;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchProviderException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import javax.ws.rs.core.Response.ResponseBuilder;
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
import us.kbase.auth2.lib.identity.RemoteIdentityWithID;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.IncomingToken;
import us.kbase.auth2.lib.token.NewToken;
import us.kbase.auth2.lib.token.TemporaryToken;

@Path("/login")
public class Login {

	//TODO TEST
	//TODO JAVADOC
	//TODO NOW add last login date
	//TODO NOW add account created date
	@Inject
	private Authentication auth;
	
	@GET
	public Response loginStart(
			@QueryParam("provider") final String provider,
			@QueryParam("redirect") final String redirect,
			@Context UriInfo uriInfo)
					throws NoSuchIdentityProviderException {
		//TODO REDIRECT check redirect url matches allowed config & is valid URL
		//TODO CONFIG allow enable & disable of id providers.
		if (provider != null && !provider.trim().isEmpty()) {
			final IdentityProvider idp = auth.getIdentityProvider(
					provider);
			final String state = auth.getBareToken();
			final URI target = toURI(idp.getLoginURL(state, false));
			
			final ResponseBuilder r = Response.seeOther(target)
					.cookie(getStateCookie(state));
			if (redirect != null && !redirect.trim().isEmpty()) {
					r.cookie(getRedirectCookie(redirect));
			}
			return r.build();
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
			if (redirect != null && !redirect.trim().isEmpty()) {
				ret.put("redirect", redirect);
			}
			return Response.ok().entity(new Viewable("/loginstart", ret))
					.build();
		}
	}

	private NewCookie getRedirectCookie(final String redirect) {
		return new NewCookie(new Cookie(
				"redirect", redirect == null ? "no redirect" : redirect,
						"/login", null),
				"redirect url", redirect == null ? 0 : 30 * 60,
						APIConstants.SECURE_COOKIES);
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
			@CookieParam("redirect") final String redirect,
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
			r = Response.seeOther(getRedirectURI(redirect, "/me"))
			//TODO NOW can't set keep me logged in here, so set in profile
					.cookie(getLoginCookie(lr.getToken(), true))
					.cookie(getStateCookie(null))
					.cookie(getRedirectCookie(null)).build();
		} else {
			r = Response.seeOther(toURI("/login/complete")).cookie(
					getLoginInProcessCookie(lr.getTemporaryToken()))
					.cookie(getStateCookie(null))
					.build();
		}
		return r;
	}
	
	private URI getRedirectURI(final String redirect, final String deflt) {
		//TODO REDIRECT check redirect url matches allowed config & is valid URL
		if (redirect != null && !redirect.trim().isEmpty()) {
			return toURI(redirect);
		}
		return toURI(deflt);
	}

	private NewCookie getLoginInProcessCookie(final TemporaryToken token) {
		return new NewCookie(new Cookie("in-process-login-token",
				token == null ? "no token" : token.getToken(), "/login", null),
				"logintoken",
				token == null ? 0 : getMaxCookieAge(token, false),
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
		for (final Entry<RemoteIdentityWithID, AuthUser> e:
				ids.getSecondaries().entrySet()) {
			final Map<String, String> s = new HashMap<>();
			s.put("id", e.getKey().getID().toString());
			s.put("prov_username", e.getKey().getUsername());
			s.put("username", e.getValue().getUserName().getName());
			secs.add(s);
		}
		if (ids.getPrimaryUser() == null) {
			ret.put("create", true);
			ret.put("id", ids.getPrimary().getID().toString());
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
			p.put("id", ids.getPrimary().getID().toString());
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
			@CookieParam("redirect") final String redirect,
			@FormParam("id") final UUID identityID)
			throws NoTokenProvidedException, AuthenticationException,
			AuthStorageException {
		
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing in-process-login-token");
		}
		final NewToken newtoken = auth.login(
				new IncomingToken(token), identityID);
		return Response.seeOther(getRedirectURI(redirect, "/me"))
				//TODO NOW can't set keep me logged in here, so set in profile
				.cookie(getLoginCookie(newtoken, true))
				.cookie(getLoginInProcessCookie(null))
				.cookie(getRedirectCookie(null)).build();
	}
	
	@POST
	@Path("/create")
	public Response createUser(
			@CookieParam("in-process-login-token") final String token,
			@CookieParam("redirect") final String redirect,
			@FormParam("id") final UUID identityID,
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
				identityID, new UserName(userName),
				fullName, email, sessionLogin, priv);
		return Response.seeOther(getRedirectURI(redirect, "/me"))
		//TODO NOW can't set keep me logged in here, so set in profile
				.cookie(getLoginCookie(newtoken, true))
				.cookie(getLoginInProcessCookie(null))
				.cookie(getRedirectCookie(null)).build();
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
