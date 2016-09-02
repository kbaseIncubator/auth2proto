package us.kbase.auth2.service;

public interface IdentityProvider {

	//TODO JAVADOC
	
	String getProviderName();
	String getLoginURI(String state);
	//X getToken(String authcode);
	
}
