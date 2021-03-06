package us.kbase.auth2.lib.storage;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.CustomRole;
import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.Role;
import us.kbase.auth2.lib.UserName;
import us.kbase.auth2.lib.UserUpdate;
import us.kbase.auth2.lib.exceptions.LinkFailedException;
import us.kbase.auth2.lib.exceptions.NoSuchTokenException;
import us.kbase.auth2.lib.exceptions.NoSuchUserException;
import us.kbase.auth2.lib.exceptions.UnLinkFailedException;
import us.kbase.auth2.lib.exceptions.UserExistsException;
import us.kbase.auth2.lib.identity.RemoteIdentity;
import us.kbase.auth2.lib.identity.RemoteIdentityWithID;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingHashedToken;
import us.kbase.auth2.lib.token.TemporaryHashedToken;

public interface AuthStorage {
	
	//TODO JAVADOC
	

	/** Create or update the root user.
	 * The password hash and salt are cleared as soon as possible.
	 * fullName, email, and created are only set when creating the root user
	 * for the first time.
	 * Custom roles are set to the null set when creating the root user, but
	 * are otherwise left alone.
	 * @param root the root user name
	 * @param fullName the root user's full name.
	 * @param email the root user's email.
	 * @param roles the root user's roles.
	 * @param created the root user account's creation date.
	 * @param passwordHash the hash of the root user's password.
	 * @param salt the password salt.
	 * @throws AuthStorageException if a problem connecting with the storage
	 * system occurs. 
	 */
	void createRoot(UserName root, String fullName, String email,
			Set<Role> roles, Date created, byte[] passwordHash,
			byte[] salt) throws AuthStorageException;
	
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
	
	void updateUser(UserName userName, UserUpdate update)
			throws NoSuchUserException, AuthStorageException;
	
	void setLastLogin(UserName userName, Date lastLogin)
			throws NoSuchUserException, AuthStorageException;
	
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

	Set<CustomRole> getCustomRoles(Set<String> roleIds)
			throws AuthStorageException;

	void setCustomRoles(UserName userName, Set<String> roles)
			throws NoSuchUserException, AuthStorageException;

	// assumes token is unique
	void storeIdentitiesTemporarily(
			TemporaryHashedToken token,
			Set<RemoteIdentityWithID> ids)
			throws AuthStorageException;

	Set<RemoteIdentityWithID> getTemporaryIdentities(
			IncomingHashedToken token)
			throws AuthStorageException, NoSuchTokenException;

	void link(UserName userName, RemoteIdentityWithID remoteID)
			throws NoSuchUserException, AuthStorageException,
			LinkFailedException;

	void unlink(UserName userName, UUID id)
			throws AuthStorageException, UnLinkFailedException;
}
