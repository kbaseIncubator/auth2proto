package us.kbase.auth2.service;

import java.util.Set;

public interface AuthConfig {

	//TODO JAVADOC
	
	SLF4JAutoLogger getLogger();
	Set<IdentityProviderConfig> getIdentityProviderConfigs();
	String getMongoHost();
	String getMongoDatabase();
	String getMongoUser();
	char[] getMongoPwd();
}
