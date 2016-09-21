package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.APIUtils.getToken;
import static us.kbase.auth2.service.api.APIUtils.getMaxCookieAge;
import static us.kbase.auth2.service.api.APIUtils.relativize;
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
import us.kbase.auth2.lib.LinkIdentities;
import us.kbase.auth2.lib.LinkToken;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.ErrorType;
import us.kbase.auth2.lib.exceptions.InvalidTokenException;
import us.kbase.auth2.lib.exceptions.LinkFailedException;
import us.kbase.auth2.lib.exceptions.MissingParameterException;
import us.kbase.auth2.lib.exceptions.NoSuchIdentityProviderException;
import us.kbase.auth2.lib.exceptions.NoTokenProvidedException;
import us.kbase.auth2.lib.identity.IdentityProvider;
import us.kbase.auth2.lib.identity.RemoteIdentityWithID;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.IncomingToken;
import us.kbase.auth2.lib.token.TemporaryToken;

@Path("/link")
public class Link {

	//TODO JAVADOC
	//TODO TEST
	//TODO CODE can probably share some code with /login
	
	@Inject
	private Authentication auth;
	
	@GET
	public Response linkStart(
			@CookieParam("token") final String token,
			@QueryParam("provider") final String provider,
			@Context UriInfo uriInfo)
			throws NoSuchIdentityProviderException, NoTokenProvidedException,
			InvalidTokenException, AuthStorageException {

		final IncomingToken incToken = getToken(token);
		
		//TODO CONFIG allow enable & disable of id providers.
		if (provider != null && !provider.trim().isEmpty()) {
			final IdentityProvider idp = auth.getIdentityProvider(
					provider);
			final String state = auth.getBareToken();
			final URI target = toURI(idp.getLoginURL(state, true));
			return Response.seeOther(target)
					.cookie(getStateCookie(state))
					.build();
		} else {
			final AuthUser u = auth.getUser(incToken);
			final Map<String, Object> ret = new HashMap<>();
			ret.put("user", u.getUserName().getName());
			ret.put("local", u.isLocal());
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
	
	@GET
	@Path("/complete/{provider}")
	public Response login(
			@PathParam("provider") String provider,
			@CookieParam("statevar") final String state,
			@CookieParam("token") final String token,
			@Context final UriInfo uriInfo)
			throws MissingParameterException, AuthenticationException,
			NoSuchProviderException, AuthStorageException,
			NoTokenProvidedException, LinkFailedException {
		//TODO INPUT handle error in params (provider, state)
		provider = upperCase(provider);
		final MultivaluedMap<String, String> qps =
				uriInfo.getQueryParameters();
		//TODO ERRHANDLE handle returned OAuth error code in queryparams
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
		final LinkToken lt = auth.link(getToken(token), provider,
				authcode);
		final Response r;
		// always redirect so the authcode doesn't remain in the title bar
		// note nginx will rewrite the redirect appropriately so absolute
		// redirects are ok
		if (lt.isLinked()) {
			r = Response.seeOther(toURI("/me"))
					.cookie(getStateCookie(null)).build();
		} else {
			r = Response.seeOther(toURI("/link/complete")).cookie(
					getLinkInProcessCookie(lt.getTemporaryToken()))
					.cookie(getStateCookie(null))
					.build();
		}
		return r;
	}
	
	private NewCookie getLinkInProcessCookie(final TemporaryToken token) {
		return new NewCookie(new Cookie("in-process-link-token",
				token == null ? "no token" : token.getToken(), "/link", null),
				"linktoken", token == null ? 0 : getMaxCookieAge(token, false),
				APIConstants.SECURE_COOKIES);
	}
	
	@GET
	@Path("/complete")
	@Template(name = "/linkchoice")
	public Map<String, Object> linkComplete(
			@CookieParam("in-process-link-token") final String linktoken,
			@CookieParam("token") final String token,
			@Context final UriInfo uriInfo)
			throws NoTokenProvidedException, AuthStorageException,
			InvalidTokenException, LinkFailedException {
		if (linktoken == null || linktoken.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing in-process-link-token");
		}
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing user token");
		}
		final LinkIdentities ids = auth.getLinkState(
				new IncomingToken(token.trim()),
				new IncomingToken(linktoken.trim()));
		/* there's a possibility here that between the redirects the number
		 * of identities that aren't already linked was reduced 1. The
		 * probability is so low that it's not worth special casing it,
		 * especially since the effect is simply that the user only has one
		 * choice for link targets.
		 */ 
		final Map<String, Object> ret = new HashMap<>();
		ret.put("user", ids.getUser().getUserName().getName());
		ret.put("provider", ids.getIdentities()
				.iterator().next().getRemoteID().getProvider());
		final List<Map<String, String>> ris = new LinkedList<>();
		ret.put("ids", ris);
		for (final RemoteIdentityWithID ri: ids.getIdentities()) {
			final Map<String, String> s = new HashMap<>();
			s.put("id", ri.getID().toString());
			s.put("prov_username", ri.getDetails().getUsername());
			ris.add(s);
		}
		ret.put("pickurl", relativize(uriInfo, "/link/pick"));
		return ret;
	}
	
	@POST
	@Path("/pick")
	public Response pickAccount(
			@CookieParam("in-process-link-token") final String linktoken,
			@CookieParam("token") final String token,
			@FormParam("id") final UUID identityID)
			throws NoTokenProvidedException, AuthenticationException,
			AuthStorageException, LinkFailedException {
		
		if (linktoken == null || linktoken.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing in-process-link-token");
		}
		if (token == null || token.trim().isEmpty()) {
			throw new NoTokenProvidedException(
					"Missing user token");
		}
		auth.link(new IncomingToken(token), new IncomingToken(linktoken),
				identityID);
		return Response.seeOther(toURI("/me"))
				.cookie(getLinkInProcessCookie(null)).build();
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
