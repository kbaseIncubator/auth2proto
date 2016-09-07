package us.kbase.auth2.lib.identity;

import java.net.URL;

public interface IdentityProvider {

	//TODO JAVADOC
	
	String getProviderName();
	String getRelativeImageURL();
	//note state will be url encoded.
	URL getLoginURL(String state);
	String getAuthCodeQueryParamName();
	//X getToken(String authcode);
	
}
