package us.kbase.auth2.lib;

import static us.kbase.auth2.lib.Utils.checkString;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import us.kbase.auth2.cryptutils.PasswordCrypt;
import us.kbase.auth2.cryptutils.TokenGenerator;
import us.kbase.auth2.lib.exceptions.ErrorType;
import us.kbase.auth2.lib.exceptions.IdentityRetrievalException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.InvalidTokenException;
import us.kbase.auth2.lib.exceptions.MissingParameterException;
import us.kbase.auth2.lib.exceptions.NoSuchIdentityProviderException;
import us.kbase.auth2.lib.exceptions.NoSuchTokenException;
import us.kbase.auth2.lib.exceptions.NoSuchUserException;
import us.kbase.auth2.lib.exceptions.UnauthorizedException;
import us.kbase.auth2.lib.exceptions.UserExistsException;
import us.kbase.auth2.lib.identity.IdentityProvider;
import us.kbase.auth2.lib.identity.IdentitySet;
import us.kbase.auth2.lib.identity.RemoteIdentity;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.NewToken;
import us.kbase.auth2.lib.token.TemporaryToken;
import us.kbase.auth2.lib.token.TokenSet;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingToken;

public class Authentication {

	//TODO TEST unit tests
	//TODO TEST test logging on startup
	//TODO TEST test logging on calls
	//TODO JAVADOC 
	//TODO AUTH schema version
	//TODO AUTH handle root user somehow (spec chars unallowed in usernames?)
	//TODO AUTH server root should return server version (and urls for endpoints?)
	//TODO AUTH check workspace for other useful things like the schema manager
	//TODO NOW logging everywhere - on login, on logout, on create / delete / expire token
	//TODO SCOPES configure scopes via ui
	//TODO SCOPES configure scope on login via ui
	//TODO SCOPES restricted scopes - allow for specific roles or users (or for specific clients via oauth2)
	//TODO ADMIN revoke user token, revoke all tokens for a user, revoke all tokens
	//TODO ADMIN deactivate account
	//TODO ADMIN force user pwd reset
	//TODO NOW tokens - redirect to standard login if not logged in (other pages as well)
	//TODO USERPROFILE email & username change propagation
	//TODO USERCONFIG set email & username privacy & respect
	//TODO USERCONFIG set email & username
	//TODO ADMIN option to allow all users to make dev token
	//TODO NOW allow redirect url on login
	
	private final AuthStorage storage;
	private final Map<String, IdentityProvider> idprov;
	private final TokenGenerator tokens;
	private final PasswordCrypt pwdcrypt;
	
	public Authentication(
			final AuthStorage storage,
			final Set<IdentityProvider> set) {
		System.out.println("starting application");
		try {
			tokens = new TokenGenerator();
			pwdcrypt = new PasswordCrypt();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("This should be impossible", e);
		}
		if (storage == null) {
			throw new NullPointerException("storage");
		}
		this.storage = storage;
		final Map<String, IdentityProvider> idp = new TreeMap<>();
		if (set != null) {
			for (final IdentityProvider id: set) {
				idp.put(id.getProviderName(), id);
			}
		}
		this.idprov = Collections.unmodifiableMap(idp);
		
	}


	public Password createLocalUser(
			final UserName userName,
			final String fullName,
			final String email)
			throws AuthStorageException, UserExistsException,
			MissingParameterException {
		if (userName == null) {
			throw new NullPointerException("userName");
		}
		checkString(fullName, "full name");
		checkString(email, "email");
		final Password pwd = new Password(tokens.getTemporaryPassword(10));
		final byte[] salt = pwdcrypt.generateSalt();
		final byte[] passwordHash = pwdcrypt.getEncryptedPassword(
				pwd.getPassword(), salt);
		final LocalUser lu = new LocalUser(userName, email, fullName, null,
				null, passwordHash, salt, true);
		//TODO NOW store creation date
		storage.createLocalUser(lu);
		return pwd;
	}
	
	public NewToken localLogin(final UserName userName, final Password pwd)
			throws AuthenticationException, AuthStorageException {
		final LocalUser u;
		try {
			u = storage.getLocalUser(userName);
		} catch (NoSuchUserException e) {
			throw new AuthenticationException(ErrorType.AUTHENTICATION_FAILED,
					"Username / password mismatch");
		}
		if (!pwdcrypt.authenticate(pwd.getPassword(), u.getPasswordHash(),
				u.getSalt())) {
			throw new AuthenticationException(ErrorType.AUTHENTICATION_FAILED,
					"Username / password mismatch");
		}
		pwd.clear();
		//TODO NOW if reset required, make reset token
		final NewToken t = new NewToken(tokens.getToken(), userName,
				//TODO CONFIG make token lifetime configurable
				new Date(new Date().getTime() + (14 * 24 * 60 * 60 * 1000)));
		storage.storeToken(t.getHashedToken());
		return t;
	}

	public TokenSet getTokens(final IncomingToken token)
			throws AuthStorageException, InvalidTokenException {
		final HashedToken ht = getToken(token);
		return new TokenSet(ht, storage.getTokens(ht.getUserName()));
	}

	// converts a no such token exception into an invalid token exception.
	public HashedToken getToken(final IncomingToken token)
			throws AuthStorageException, InvalidTokenException {
		if (token == null) {
			throw new NullPointerException("token");
		}
		try {
			return storage.getToken(token.getHashedToken());
		} catch (NoSuchTokenException e) {
			throw new InvalidTokenException();
		}
	}

	public NewToken createToken(
			final IncomingToken token,
			final String tokenName,
			final boolean serverToken)
			throws AuthStorageException, MissingParameterException,
			InvalidTokenException, UnauthorizedException {
		checkString(tokenName, "token name");
		final AuthUser au = getUser(token);
		final Role reqRole = serverToken ? Role.SERV_TOKEN : Role.DEV_TOKEN;
		if (!Role.hasRole(au.getRoles(), reqRole)) {
			throw new UnauthorizedException(ErrorType.UNAUTHORIZED,
					"User %s is not authorized to create this token type.");
		}
		final long life;
		//TODO CONFIG make token lifetime configurable
		if (serverToken) {
			life = Long.MAX_VALUE;
		} else {
			life = 90L * 24L * 60L * 60L * 1000L;
		}
		final long now = new Date().getTime();
		final long exp;
		if (Long.MAX_VALUE - life < now) {
			exp = Long.MAX_VALUE;
		} else {
			exp = now + life;
		}
		final NewToken t = new NewToken(tokenName, tokens.getToken(),
				au.getUserName(), new Date(exp));
		storage.storeToken(t.getHashedToken());
		return t;
	}
	
	// gets user for token
	public AuthUser getUser(final IncomingToken token)
			throws AuthStorageException, InvalidTokenException {
		final HashedToken ht = getToken(token);
		try {
			return storage.getUser(ht.getUserName());
		} catch (NoSuchUserException e) {
			throw new RuntimeException("There seems to be an error in the " +
					"storage system. Token was valid, but no user", e);
		}
	}

	// get a (possibly) different user 
	public AuthUser getUser(
			final IncomingToken token,
			final UserName user)
			throws AuthStorageException, InvalidTokenException,
			NoSuchUserException {
		final HashedToken ht = getToken(token);
		final AuthUser u = storage.getUser(user);
		if (ht.getUserName().equals(u.getUserName())) {
			return u;
		} else {
			//TODO NOW this shouldn't return roles
			//TODO NOW only return fullname & email if info is public - actually, never return email
			return u;
		}
	}

	public void revokeToken(
			final IncomingToken token,
			final UUID tokenId)
			throws AuthStorageException,
			NoSuchTokenException, InvalidTokenException {
		final HashedToken ht = getToken(token);
		storage.deleteToken(ht.getUserName(), tokenId);
	}
	
	//note returns null if the token could not be found 
	public HashedToken revokeToken(final IncomingToken token)
			throws AuthStorageException {
		if (token == null) {
			throw new NullPointerException("token");
		}
		HashedToken ht = null;
		try {
			ht = storage.getToken(token.getHashedToken());
			storage.deleteToken(ht.getUserName(), ht.getId());
		} catch (NoSuchTokenException e) {
			// no problem, continue
		}
		return ht;
	}

	public void revokeTokens(final IncomingToken token)
			throws AuthStorageException, InvalidTokenException {
		final HashedToken ht = getToken(token);
		storage.deleteTokens(ht.getUserName());
	}


	public AuthUser getUserAsAdmin(
			final IncomingToken adminToken,
			final UserName userName)
			throws AuthStorageException, NoSuchUserException {
		if (userName == null) {
			throw new NullPointerException("userName");
		}
		//TODO ADMIN check user is admin
		return storage.getUser(userName);
	}


	public void updateRoles(
			final IncomingToken adminToken,
			final UserName userName,
			final Set<Role> roles)
			throws NoSuchUserException, AuthStorageException {
		//TODO ADMIN check user is admin
		for (final Role r: roles) {
			if (r == null) {
				throw new NullPointerException("no null roles");
			}
		}
		if (userName == null) {
			throw new NullPointerException("userName");
		}
		storage.setRoles(userName, roles);
	}

	public void setCustomRole(
			final IncomingToken incomingToken,
			final String name,
			final String description)
			throws MissingParameterException, AuthStorageException {
		//TODO ADMIN check user is admin
		storage.setCustomRole(new CustomRole(
				UUID.randomUUID(), name, description));
	}

	public Set<CustomRole> getCustomRoles(final IncomingToken incomingToken)
			throws AuthStorageException {
		//TODO ADMIN check user is admin
		return storage.getCustomRoles();
	}

	public void updateCustomRoles(
			final IncomingToken adminToken,
			final UserName userName,
			final Set<UUID> roleIds)
			throws AuthStorageException, NoSuchUserException {
		//TODO ADMIN check user is admin
		final Set<CustomRole> roles = storage.getCustomRoles(roleIds);
		final Set<String> rstr = roles.stream().map(r -> r.getName())
				.collect(Collectors.toSet());
		storage.setCustomRoles(userName, rstr);
	}


	public List<IdentityProvider> getIdentityProviders() {
		return new LinkedList<IdentityProvider>(idprov.values());
	}

	// note not saved in DB
	public String getBareToken() {
		return tokens.getToken();
	}


	public IdentityProvider getIdentityProvider(final String provider)
			throws NoSuchIdentityProviderException {
		if (!idprov.containsKey(provider)) {
			throw new NoSuchIdentityProviderException(provider); 
		}
		return idprov.get(provider);
	}

	// split from getloginstate since the user may need to make a choice
	// we assume that this is via a html page and therefore a redirect should
	// occur before said choice to hide the authcode, hence the temporary
	// token instead of returning the choices directly
	public LoginToken login(final String provider, final String authcode)
			throws NoSuchProviderException, MissingParameterException,
			IdentityRetrievalException, AuthStorageException {
		final IdentityProvider idp = idprov.get(provider);
		if (idp == null) {
			throw new NoSuchProviderException(provider);
		}
		if (authcode == null || authcode.trim().isEmpty()) {
			throw new MissingParameterException("authorization code");
		}
		final IdentitySet ids = idp.getIdentities(authcode);
		final AuthUser primary = storage.getUser(ids.getPrimary());
		final Set<RemoteIdentity> filteredIDs = ids.getSecondaries().stream()
				.filter(id -> storage.hasUser(id)).collect(Collectors.toSet());
		final LoginToken lr;
		if (primary == null || !filteredIDs.isEmpty()) {
			final int expmin = primary == null ? 30 : 10;
			final TemporaryToken tt = new TemporaryToken(tokens.getToken(),
					new Date(new Date().getTime() + (expmin * 60 * 1000)));
			filteredIDs.add(ids.getPrimary());
			storage.storeIdentitiesTemporarily(
					tt.getHashedToken(), filteredIDs);
			lr = new LoginToken(tt);
		} else {
			//TODO NOW if reset required, make reset token
			final NewToken t = new NewToken(tokens.getToken(),
					primary.getUserName(),
					//TODO CONFIG make token lifetime configurable
					new Date(new Date().getTime() + (14 * 24 * 60 * 60 * 1000)));
			storage.storeToken(t.getHashedToken());
			lr = new LoginToken(t);
		}
		return lr;
		//TODO NOW provide choice to create kbase id for primary if not already, and provide choices to login as secondaries
	}


	public LoginIdentities getLoginState(final IncomingToken token)
			throws AuthStorageException, InvalidTokenException {
		if (token == null) {
			throw new NullPointerException("token");
		}
		final Set<RemoteIdentity> ids = getTemporaryIdentities(token);
		RemoteIdentity primary = null;
		String provider = null;
		for (final RemoteIdentity ri: ids) {
			if (provider == null) {
				provider = ri.getProvider();
			} else if (!provider.equals(ri.getProvider())) {
				throw new AuthStorageException("More than one identity " +
						"provider associated with this token");
			}
			if (ri.isPrimary()) {
				if (primary != null) {
					throw new AuthStorageException("More than one primary " +
							"identity associated with this token");
				}
				primary = ri;
			}
		}
		if (primary == null) {
			throw new AuthStorageException(
					"No primary identity associated with this token");
		}
		ids.remove(primary);
		final AuthUser pu = storage.getUser(primary);
		final Map<RemoteIdentity, AuthUser> secs = processSecondaries(ids);
		return new LoginIdentities(primary, pu, secs);
	}


	private Set<RemoteIdentity> getTemporaryIdentities(
			final IncomingToken token)
			throws AuthStorageException, InvalidTokenException {
		final Set<RemoteIdentity> ids;
		try {
			ids = storage.getTemporaryIdentities(
					token.getHashedToken());
		} catch (NoSuchTokenException e) {
			throw new InvalidTokenException();
		}
		return ids;
	}


	private Map<RemoteIdentity, AuthUser> processSecondaries(
			final Set<RemoteIdentity> ids)
			throws AuthStorageException {
		final Map<RemoteIdentity, AuthUser> ret = new HashMap<>();
		for (final RemoteIdentity ri: ids) {
			final AuthUser u = storage.getUser(ri);
			if (u != null && !ret.containsValue(u)) {
				ret.put(ri, u);
			}
		}
		return ret;
	}

	public NewToken createUser(
			final IncomingToken token,
			final String provider,
			final String remoteID,
			final UserName userName,
			final String fullName,
			final String email,
			final boolean sessionLogin,
			final boolean privateNameEmail)
			throws AuthStorageException, AuthenticationException,
				UserExistsException {
		//TODO NOW handle sessionLogin, privateNameEmail
		
		final Set<RemoteIdentity> ids = getTemporaryIdentities(token);
		RemoteIdentity match = null;
		for (final RemoteIdentity ri: ids) {
			if (ri.getProvider().equals(provider) &&
					ri.getId().equals(remoteID)) {
				match = ri;
			}
		}
		if (match == null) {
			throw new AuthenticationException(ErrorType.AUTHENTICATION_FAILED,
					"Not authorized to create account linked to provided identity");
		}
		storage.createUser(new AuthUser(userName, email, fullName,
				new HashSet<>(Arrays.asList(match)), null, null));
		final NewToken nt = new NewToken(tokens.getToken(), userName,
				//TODO CONFIG make token lifetime configurable
				new Date(14 * 24 * 60 * 60 * 1000));
		storage.storeToken(nt.getHashedToken());
		return nt;
	}
}
