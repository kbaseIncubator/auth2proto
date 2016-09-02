package us.kbase.auth2.service;

import java.util.Set;

public interface AuthConfig {

	//TODO JAVADOC
	
	SLF4JAutoLogger getLogger();
	Set<IdentityProvider> getIdentityProviders();
	String getMongoHost();
	String getMongoDatabase();
	String getMongoUser();
	char[] getMongoPwd();
}
