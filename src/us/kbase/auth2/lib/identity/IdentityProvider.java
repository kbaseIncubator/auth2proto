package us.kbase.auth2.lib.identity;

import java.net.URI;
import java.net.URL;

import us.kbase.auth2.lib.exceptions.IdentityRetrievalException;

public interface IdentityProvider {

	//TODO JAVADOC
	
	String getProviderName();
	URI getImageURI();
	//note state will be url encoded.
	URL getLoginURL(String state);
	String getAuthCodeQueryParamName();
	IdentitySet getIdentities(String authcode)
			throws IdentityRetrievalException;
	
}
