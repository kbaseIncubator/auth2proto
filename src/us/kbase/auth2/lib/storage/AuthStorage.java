package us.kbase.auth2.lib.storage;

import us.kbase.auth2.lib.LocalUser;

public interface AuthStorage {
	
	//TODO JAVADOC
	
	void createLocalAccount(LocalUser local);

}
