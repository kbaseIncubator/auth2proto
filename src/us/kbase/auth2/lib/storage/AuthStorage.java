package us.kbase.auth2.lib.storage;

import java.util.List;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.UserName;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingHashedToken;

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

	AuthUser getUser(UserName userName)
			throws AuthenticationException, AuthStorageException;

	LocalUser getLocalUser(UserName userName)
			throws AuthStorageException, AuthenticationException;

	HashedToken getToken(IncomingHashedToken token)
			throws AuthStorageException;

	List<HashedToken> getTokens(UserName userName) throws AuthStorageException;

}
