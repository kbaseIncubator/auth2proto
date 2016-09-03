package us.kbase.auth2.service.identity;

import java.net.URL;

public interface IdentityProvider {

	//TODO JAVADOC
	
	String getProviderName();
	URL getLoginURI(String state);
	//X getToken(String authcode);
	
}
