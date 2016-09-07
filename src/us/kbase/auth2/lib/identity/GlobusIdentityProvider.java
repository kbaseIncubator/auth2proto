package us.kbase.auth2.lib.identity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.core.UriBuilder;

public class GlobusIdentityProvider implements IdentityProvider {

	public static final String NAME = "Globus";
	private static final String SCOPE =
			"urn:globus:auth:scope:auth.globus.org:view_identities " + 
			"email";
	private static final String LOGIN_PATH = "/v2/oauth2/authorize";
	private static final String AUTH_CODE_PARAM = "code";
	
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
}
