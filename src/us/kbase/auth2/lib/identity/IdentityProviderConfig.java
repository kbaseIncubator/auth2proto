package us.kbase.auth2.lib.identity;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class IdentityProviderConfig {
	
	//TODO JAVADOC
	//TODO TEST
	
	private final String identityProviderName;
	private final String clientID;
	private final String clientSecrect;
	private final URI imageURI;
	private final URL redirectURL;
	private final URL baseURL;
	
	public IdentityProviderConfig(
			final String identityProviderName,
			final URL baseURL,
			final String clientID,
			final String clientSecrect,
			final URI imgURI,
			final URL redirectURL) {
		super();
		//TODO NOW check for nulls & empty strings
		this.identityProviderName = identityProviderName;
		this.clientID = clientID;
		this.clientSecrect = clientSecrect;
		this.imageURI = imgURI;
		this.redirectURL = redirectURL;
		this.baseURL = baseURL;
		checkValidURI(this.redirectURL, "Redirect url");
		checkValidURI(this.baseURL, "Base url");
		
	}
	private void checkValidURI(final URL url, final String name) {
		try {
			url.toURI();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(String.format(
					"%s %s for %s identity provider is not a valid URI: %s",
					name, url, identityProviderName, e.getMessage()), e);
		}
	}

	public String getIdentityProviderName() {
		return identityProviderName;
	}

	public URL getBaseURL() {
		return baseURL;
	}

	public String getClientID() {
		return clientID;
	}

	public String getClientSecrect() {
		return clientSecrect;
	}

	public URI getImageURI() {
		return imageURI;
	}

	public URL getRedirectURL() {
		return redirectURL;
	}
}
