package us.kbase.auth2.lib.storage;

import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.service.exceptions.AuthException;

public interface AuthStorage {
	
	//TODO JAVADOC
	
	void createLocalAccount(LocalUser local)
			throws AuthException, AuthStorageException;

}
