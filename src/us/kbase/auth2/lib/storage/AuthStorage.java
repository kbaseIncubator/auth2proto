package us.kbase.auth2.lib.storage;

import java.util.Set;
import java.util.UUID;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.CustomRole;
import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.Role;
import us.kbase.auth2.lib.UserName;
import us.kbase.auth2.lib.exceptions.NoSuchTokenException;
import us.kbase.auth2.lib.exceptions.NoSuchUserException;
import us.kbase.auth2.lib.exceptions.UserExistsException;
import us.kbase.auth2.lib.identity.RemoteIdentity;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingHashedToken;
import us.kbase.auth2.lib.token.TemporaryHashedToken;

public interface AuthStorage {
	
	//TODO JAVADOC
	
	/** Create a new local account. Note that new accounts are always created
	 * with no roles.
	 * @param local the user to create.
	 * @throws AuthStorageException if a problem connecting with the storage
	 * system occurs.
	 * @throws UserExistsException if the user already exists.
	 */
	void createLocalUser(LocalUser local)
			throws AuthStorageException, UserExistsException;

	void createUser(AuthUser authUser)
			throws UserExistsException, AuthStorageException;

	AuthUser getUser(UserName userName)
			throws AuthStorageException, NoSuchUserException;
	
	// returns null if no user
	// updates identity info if different from db (other than provider & id)
	AuthUser getUser(RemoteIdentity remoteID) throws AuthStorageException;

	LocalUser getLocalUser(UserName userName)
			throws AuthStorageException, NoSuchUserException;
	
	/** Store a token in the database. No checking is done on the validity
	 * of the token - passing in tokens with bad data is a programming error.
	 * @param t the token to store.
	 * @throws AuthStorageException if the token could not be stored.
	 */
	void storeToken(HashedToken t) throws AuthStorageException;

	HashedToken getToken(IncomingHashedToken token)
			throws AuthStorageException, NoSuchTokenException;

	Set<HashedToken> getTokens(UserName userName) throws AuthStorageException;

	void deleteToken(UserName userName, UUID tokenId)
			throws AuthStorageException, NoSuchTokenException;

	void deleteTokens(UserName userName) throws AuthStorageException;

	void setRoles(UserName userName, Set<Role> roles)
			throws AuthStorageException, NoSuchUserException;

	void setCustomRole(CustomRole role) throws AuthStorageException;

	Set<CustomRole> getCustomRoles() throws AuthStorageException;

	Set<CustomRole> getCustomRoles(Set<UUID> roleIds)
			throws AuthStorageException;

	void setCustomRoles(UserName userName, Set<String> roles)
			throws NoSuchUserException, AuthStorageException;

	// assumes token is unique
	void storeIdentitiesTemporarily(
			TemporaryHashedToken token,
			Set<RemoteIdentity> ids)
			throws AuthStorageException;

	Set<RemoteIdentity> getTemporaryIdentities(
			IncomingHashedToken token)
			throws AuthStorageException, NoSuchTokenException;
}
