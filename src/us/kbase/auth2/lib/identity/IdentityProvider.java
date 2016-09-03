package us.kbase.auth2.lib.identity;

import java.net.URL;

public interface IdentityProvider {

	//TODO JAVADOC
	
	String getProviderName();
	String getRelativeImageURL();
	URL getLoginURI(String state);
	//X getToken(String authcode);
	
}
