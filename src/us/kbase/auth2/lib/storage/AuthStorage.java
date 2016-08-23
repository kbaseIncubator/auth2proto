package us.kbase.auth2.lib.storage;

import us.kbase.auth2.lib.AuthToken;
import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.service.exceptions.AuthException;
import us.kbase.auth2.service.exceptions.AuthenticationException;

public interface AuthStorage {
	
	//TODO JAVADOC
	
	void createLocalAccount(LocalUser local)
			throws AuthException, AuthStorageException;

	/** Store a token in the database. No checking is done on the validity
	 * of the token - passing in tokens with bad data is a programming error.
	 * @param t the token to store.
	 * @throws AuthStorageException if the token could not be stored.
	 */
	void storeToken(AuthToken t) throws AuthStorageException;

	LocalUser getLocalUser(String userName)
			throws AuthStorageException, AuthenticationException;

}
