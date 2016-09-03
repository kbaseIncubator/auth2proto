package us.kbase.auth2.lib.identity;

import java.net.URL;

public class IdentityProviderConfig {
	
	//TODO JAVADOC
	//TODO TEST
	
	private final String identityProviderName;
	private final String clientID;
	private final String clientSecrect;
	private final String relativeImageURL;
	private final URL redirectURL;
	
	public IdentityProviderConfig(
			final String identityProviderName,
			final String clientID,
			final String clientSecrect,
			final String relativeImageURL,
			final URL redirectURL) {
		super();
		//TODO NOW check for nulls & empty strings
		this.identityProviderName = identityProviderName;
		this.clientID = clientID;
		this.clientSecrect = clientSecrect;
		this.relativeImageURL = relativeImageURL;
		this.redirectURL = redirectURL;
	}

	public String getIdentityProviderName() {
		return identityProviderName;
	}

	public String getClientID() {
		return clientID;
	}

	public String getClientSecrect() {
		return clientSecrect;
	}

	public String getRelativeImageURL() {
		return relativeImageURL;
	}

	public URL getRedirectURL() {
		return redirectURL;
	}
	
	
}
