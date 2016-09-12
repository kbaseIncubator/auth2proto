package us.kbase.auth2.lib.identity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
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
		// TODO Auto-generated method stub
		return null;
	}

}
