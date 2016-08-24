package us.kbase.auth2.lib;

import static us.kbase.auth2.lib.Utils.checkString;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import us.kbase.auth2.cryptutils.PasswordCrypt;
import us.kbase.auth2.cryptutils.TokenGenerator;
import us.kbase.auth2.lib.exceptions.AuthError;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.storage.exceptions.NoSuchTokenException;
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


	private HashedToken getToken(final IncomingToken token)
			throws AuthStorageException, AuthenticationException {
		final HashedToken ht;
		try {
			ht = storage.getToken(token.getHashedToken());
		} catch (NoSuchTokenException e) {
			throw new AuthenticationException(AuthError.INVALID_TOKEN, null);
		}
		return ht;
	}


	public NewToken createToken(
			final IncomingToken token,
			final String tokenName,
			final boolean serverToken)
			throws AuthException, AuthStorageException {
		checkString(tokenName, "token name");
		final HashedToken ht = getToken(token);
		//TODO NOW check user has rights to create dev or server token
		final NewToken t = new NewToken(tokenName, tokens.getToken(),
				ht.getUserName(),
				//TODO CONFIG make token lifetime configurable & based on type
				new Date(new Date().getTime() + (14 * 24 * 60 * 60 * 1000)));
		storage.storeToken(t.getHashedToken());
		return t;
	}
}
