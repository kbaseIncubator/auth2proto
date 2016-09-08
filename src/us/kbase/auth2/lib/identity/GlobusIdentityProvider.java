package us.kbase.auth2.lib.identity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import us.kbase.auth2.lib.exceptions.IdentityRetrievalException;

public class GlobusIdentityProvider implements IdentityProvider {

	
	//TODO TEST
	//TODO JAVADOC
	
	public static final String NAME = "Globus";
	private static final String SCOPE =
			"urn:globus:auth:scope:auth.globus.org:view_identities " + 
			"email";
	private static final String LOGIN_PATH = "/v2/oauth2/authorize";
	private static final String TOKEN_PATH = "/v2/oauth2/token";
	private static final String INTROSPECT_PATH = TOKEN_PATH + "/introspect";
	private static final String AUTH_CODE_PARAM = "code";
	
	//thread safe
	private static final Client cli = ClientBuilder.newClient();
	
	private final IdentityProviderConfig cfg;
	
	public GlobusIdentityProvider(final IdentityProviderConfig idc) {
		if (idc == null) {
			throw new NullPointerException("idc");
		}
		if (!NAME.equals(idc.getIdentityProviderName())) {
			throw new IllegalArgumentException("Bad config name: " +
					idc.getIdentityProviderName());
		}
		this.cfg = idc;
	}
	
	@Override
	public String getProviderName() {
		return NAME;
	}
	
	@Override
	public String getRelativeImageURL() {
		return cfg.getRelativeImageURL();
	}

	// state will be url encoded.
	@Override
	public URL getLoginURL(final String state) {
		final URI target = UriBuilder.fromUri(toURI(cfg.getBaseURL()))
				.path(LOGIN_PATH)
				.queryParam("scope", SCOPE)
				.queryParam("state", state)
				.queryParam("redirect_uri", cfg.getRedirectURL())
				.queryParam("response_type", "code")
				.queryParam("client_id", cfg.getClientID())
				.build();
		return toURL(target);
	}
	
	@Override
	public String getAuthCodeQueryParamName() {
		return AUTH_CODE_PARAM;
	}

	//Assumes valid URL in URI form
	private URL toURL(final URI baseURI) {
		try {
			return baseURI.toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("This should be impossible", e);
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

	@Override
	public IdentitySet getIdentities(final String accessToken)
			throws IdentityRetrievalException {
		/* Note authcode only works once. After that globus returns
		 * {error=invalid_grant}
		 */
		
		final URI target = UriBuilder.fromUri(toURI(cfg.getBaseURL()))
				.path(INTROSPECT_PATH).build();
		
		final MultivaluedMap<String, String> formParameters =
				new MultivaluedHashMap<>();
		formParameters.add("token", accessToken);
		formParameters.add("include", "identities_set");
		
		final Map<String, Object> m = globusRequest(formParameters, target);
		System.out.println(m);
		@SuppressWarnings("unchecked")
		final List<String> audience = (List<String>) m.get("aud");
		// per Globus spec, check that the audience for the requests includes
		// our client
		if (!audience.contains(cfg.getClientID())) {
			throw new IdentityRetrievalException(
					"The audience for the Globus request does not include " +
					"this client");
		}
		final String primary = (String) m.get("sub");
		@SuppressWarnings("unchecked")
		final List<String> secondary = (List<String>) m.get("identities_set");
		secondary.remove(primary);
		System.out.println(primary);
		System.out.println(secondary);
		
		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAccessToken(final String authcode) {
		
		final MultivaluedMap<String, String> formParameters =
				new MultivaluedHashMap<>();
		formParameters.add("code", authcode);
		formParameters.add("redirect_uri", cfg.getRedirectURL().toString());
		formParameters.add("grant_type", "authorization_code");
		
		final URI target = UriBuilder.fromUri(toURI(cfg.getBaseURL()))
				.path(TOKEN_PATH).build();
		
		final Map<String, Object> m = globusRequest(formParameters, target);
		return (String) m.get("access_token");
	}

	private Map<String, Object> globusRequest(
			final MultivaluedMap<String, String> formParameters,
			final URI target) {
		final String bauth = "Basic " + Base64.getEncoder().encodeToString(
				(cfg.getClientID() + ":" + cfg.getClientSecrect()).getBytes());
		final WebTarget wt = cli.target(target);
		Response r = null;
		try {
			r = wt.request(MediaType.APPLICATION_JSON_TYPE)
					.header("Authorization", bauth)
					.post(Entity.form(formParameters));
			@SuppressWarnings("unchecked")
			final Map<String, Object> mtemp = r.readEntity(Map.class);
			//TODO NOW handle {error=?} in object and check response code
			return mtemp;
		} finally {
			if (r != null) {
				r.close();
			}
		}
	}
}
