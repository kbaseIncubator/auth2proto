package us.kbase.auth2.lib.storage;

import java.util.List;

import us.kbase.auth2.lib.HashedToken;
import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.IncomingHashedToken;
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
	void storeToken(HashedToken t) throws AuthStorageException;

	LocalUser getLocalUser(String userName)
			throws AuthStorageException, AuthenticationException;

	HashedToken getToken(IncomingHashedToken incomingToken) throws AuthStorageException;

	List<HashedToken> getTokens(String userName) throws AuthStorageException;

}
