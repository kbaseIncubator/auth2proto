package us.kbase.auth2.lib.identity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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

public class GoogleIdentityProvider implements IdentityProvider {

	//TODO TEST
	//TODO JAVADOC
	//TODO NOW look for common methods between Google and Globus
	
	
	/* Get creds: https://console.developers.google.com/apis
	 * ( need to enable google+)?
	 * Docs:
	 * https://developers.google.com/identity/protocols/OAuth2WebServer
	 * https://developers.google.com/+/web/api/rest/oauth#login-scopes
	 * https://developers.google.com/+/web/api/rest/latest/people/get
	 * https://developers.google.com/+/web/api/rest/latest/people
	 */
	
	public static final String NAME = "Google";
	private static final String SCOPE =
			"https://www.googleapis.com/auth/plus.me profile";
	private static final String LOGIN_PATH = "/o/oauth2/v2/auth";
	private static final String TOKEN_PATH = "/oauth2/v4/token";
	private static final String AUTH_CODE_PARAM = "code";
	
	//thread safe
	private static final Client CLI = ClientBuilder.newClient();
	
	private final IdentityProviderConfig cfg;
	
	public GoogleIdentityProvider(final IdentityProviderConfig idc) {
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
	public URI getImageURI() {
		return cfg.getImageURI();
	}

	// state will be url encoded
	@Override
	public URL getLoginURL(final String state) {
		final URI target = UriBuilder.fromUri(toURI(cfg.getLoginURL()))
				.path(LOGIN_PATH)
				.queryParam("scope", SCOPE)
				.queryParam("state", state)
				.queryParam("redirect_uri", cfg.getLoginRedirectURL())
				.queryParam("response_type", "code")
				.queryParam("client_id", cfg.getClientID())
				.queryParam("prompt", "select_account")
				.build();
		return toURL(target);
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
	public String getAuthCodeQueryParamName() {
		return AUTH_CODE_PARAM;
	}

	@Override
	public IdentitySet getIdentities(final String authcode) {
		//TODO NOW will need to handle link vs. login case
		final String accessToken = getAccessToken(authcode);
		final RemoteIdentity ri = getIdentity(accessToken);
		return new IdentitySet(ri, null);
	}

	private RemoteIdentity getIdentity(final String accessToken) {
		
		// TODO Auto-generated method stub
		return null;
	}

	private String getAccessToken(final String authcode) {
		final MultivaluedMap<String, String> formParameters =
				new MultivaluedHashMap<>();
		formParameters.add("code", authcode);
		formParameters.add("redirect_uri",
				cfg.getLoginRedirectURL().toString());
		formParameters.add("grant_type", "authorization_code");
		formParameters.add("client_id", cfg.getClientID());
		formParameters.add("client_secret", cfg.getClientSecrect());
		
		final URI target = UriBuilder.fromUri(toURI(cfg.getApiURL()))
				.path(TOKEN_PATH).build();
		
		final Map<String, Object> m = googlePostRequest(
				formParameters, target);
		System.out.println(m);
		return (String) m.get("access_token");
	}

	private Map<String, Object> googlePostRequest(
			final MultivaluedMap<String, String> formParameters,
			final URI target) {
		final WebTarget wt = CLI.target(target);
		Response r = null;
		try {
			r = wt.request(MediaType.APPLICATION_JSON_TYPE)
					.post(Entity.form(formParameters));
			@SuppressWarnings("unchecked")
			//TODO TEST with 500s with HTML
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
