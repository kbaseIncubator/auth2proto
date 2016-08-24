package us.kbase.auth2.lib;

import static us.kbase.auth2.lib.Utils.checkString;
import static us.kbase.auth2.lib.Utils.clear;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import us.kbase.auth2.cryptutils.PasswordCrypt;
import us.kbase.auth2.cryptutils.TokenGenerator;
import us.kbase.auth2.lib.exceptions.AuthError;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.storage.exceptions.NoSuchTokenException;
import us.kbase.auth2.lib.token.AuthToken;
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


	public char[] createLocalUser(
			final String userName,
			final String fullName,
			final String email)
			throws AuthException, AuthStorageException {
		//TODO NOW check minimum user name length, check email
		checkString(userName, "user name");
		checkString(fullName, "full name");
		checkString(email, "email");
		final char[] pwd = tokens.getTemporaryPassword(10);
		final byte[] salt = pwdcrypt.generateSalt();
		final byte[] passwordHash = pwdcrypt.getEncryptedPassword(pwd, salt);
		final LocalUser lu = new LocalUser(userName, email, fullName,
				passwordHash, salt, true);
		storage.createLocalAccount(lu);
		return pwd;
	}
	
	public AuthToken localLogin(final String userName, final char[] pwd)
			throws AuthenticationException, AuthStorageException {
		final LocalUser u = storage.getLocalUser(userName);
		if (!pwdcrypt.authenticate(pwd, u.getPasswordHash(), u.getSalt())) {
			throw new AuthenticationException(AuthError.AUTHENICATION_FAILED,
					"Username / password mismatch");
		}
		clear(pwd);
		//TODO NOW if reset required, make reset token
		final AuthToken t = new AuthToken(tokens.getToken(), userName,
				//TODO CONFIG make token lifetime configurable
				new Date(new Date().getTime() + (14 * 24 * 60 * 60 * 1000)));
		storage.storeToken(t.getHashedToken());
		return t;
	}

	public List<HashedToken> getTokens(final IncomingToken token)
			throws AuthenticationException, AuthStorageException {
		final HashedToken ht;
		try {
			ht = storage.getToken(token.getHashedToken());
		} catch (NoSuchTokenException e) {
			throw new AuthenticationException(AuthError.INVALID_TOKEN, null);
		}
		return storage.getTokens(ht.getUserName());
	}
}
