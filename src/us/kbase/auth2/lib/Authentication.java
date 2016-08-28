package us.kbase.auth2.lib;

import static us.kbase.auth2.lib.Utils.checkString;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

import us.kbase.auth2.cryptutils.PasswordCrypt;
import us.kbase.auth2.cryptutils.TokenGenerator;
import us.kbase.auth2.lib.exceptions.AuthError;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.NoSuchTokenException;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.NewToken;
import us.kbase.auth2.lib.token.TokenSet;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingToken;

public class Authentication {

	//TODO TEST unit tests
	//TODO JAVADOC 
	//TODO AUTH schema version
	//TODO AUTH handle root user somehow (spec chars unallowed in usernames?)
	//TODO AUTH server root should return server version (and urls for endpoints?)
	//TODO AUTH check workspace for other useful things like the schema manager
	//TODO NOW logging everywhere - on login, on logout, on create / delete / expire token
	//TODO NOW roles - Admin, CreateDevToken, CreateServerToken
	//TODO NOW custom roles set up via ui
	//TODO SCOPES configure scopes via ui
	//TODO SCOPES configure scope on login via ui
	//TODO SCOPES restricted scopes - allow for specific roles or users (or for specific clients via oauth2)
	//TODO ADMIN revoke user token, revoke all tokens for a user, revoke all tokens
	//TODO ADMIN deactivate account
	//TODO ADMIN force user pwd reset
	//TODO NOW tokens - redirect to standard login if not logged in (other pages as well)
	
	private final AuthStorage storage;
	private final TokenGenerator tokens;
	private final PasswordCrypt pwdcrypt;
	
	public Authentication(final AuthStorage storage) {
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
	}


	public Password createLocalUser(
			final UserName userName,
			final String fullName,
			final String email)
			throws AuthException, AuthStorageException {
		if (userName == null) {
			throw new NullPointerException("userName");
		}
		checkString(fullName, "full name");
		checkString(email, "email");
		final Password pwd = new Password(tokens.getTemporaryPassword(10));
		final byte[] salt = pwdcrypt.generateSalt();
		final byte[] passwordHash = pwdcrypt.getEncryptedPassword(
				pwd.getPassword(), salt);
		final LocalUser lu = new LocalUser(userName, email, fullName,
				passwordHash, salt, true);
		//TODO NOW store creation date
		storage.createLocalAccount(lu);
		return pwd;
	}
	
	public NewToken localLogin(final UserName userName, final Password pwd)
			throws AuthenticationException, AuthStorageException {
		final LocalUser u = storage.getLocalUser(userName);
		if (!pwdcrypt.authenticate(pwd.getPassword(), u.getPasswordHash(),
				u.getSalt())) {
			throw new AuthenticationException(AuthError.AUTHENICATION_FAILED,
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
			throws AuthenticationException, AuthStorageException {
		final HashedToken ht = getToken(token);
		return new TokenSet(ht, storage.getTokens(ht.getUserName()));
	}

	// converts a no such token exception into an invalid token exception.
	public HashedToken getToken(final IncomingToken token)
			throws AuthStorageException, AuthenticationException {
		if (token == null) {
			throw new NullPointerException("token");
		}
		try {
			return storage.getToken(token.getHashedToken());
		} catch (NoSuchTokenException e) {
			throw new AuthenticationException(AuthError.INVALID_TOKEN, null);
		}
	}

	public NewToken createToken(
			final IncomingToken token,
			final String tokenName,
			final boolean serverToken)
			throws AuthException, AuthStorageException {
		checkString(tokenName, "token name");
		final HashedToken ht = getToken(token);
		//TODO NOW check user has rights to create dev or server token
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
				ht.getUserName(), new Date(exp));
		storage.storeToken(t.getHashedToken());
		return t;
	}
	
	// gets user for token
	public AuthUser getUser(final IncomingToken token)
			throws AuthenticationException, AuthStorageException {
		final HashedToken ht = getToken(token);
		return storage.getUser(ht.getUserName());
	}

	// get a (possibly) different user 
	public AuthUser getUser(
			final IncomingToken token,
			final UserName user)
			throws AuthenticationException, AuthStorageException {
		final HashedToken ht = getToken(token);
		final AuthUser u = storage.getUser(user);
		if (ht.getUserName().equals(u.getUserName())) {
			return u;
		} else {
			//TODO NOW only return fullname & email if info is public - actually, never return email
			return u;
		}
	}

	public void revokeToken(
			final IncomingToken token,
			final UUID tokenId)
			throws AuthenticationException, AuthStorageException,
			NoSuchTokenException {
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
			throws AuthenticationException, AuthStorageException {
		final HashedToken ht = getToken(token);
		storage.deleteTokens(ht.getUserName());
	}

}
