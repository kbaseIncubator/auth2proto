package us.kbase.auth2.lib;

import static us.kbase.auth2.lib.Utils.clear;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import us.kbase.auth2.cryptutils.PasswordCrypt;
import us.kbase.auth2.cryptutils.TokenGenerator;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.service.exceptions.AuthException;
import us.kbase.auth2.service.exceptions.MissingParameterException;

public class Authentication {

	//TODO TEST unit tests
	//TODO JAVADOC 
	
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
	
	private void checkString(final String s, final String name)
			throws MissingParameterException {
		if (s == null || s.isEmpty()) {
			throw new MissingParameterException("Missing parameter: " + name);
		}
	}


	public AuthToken localLogin(final String userName, final char[] pwd) {
		final AuthToken t = new AuthToken("faketoken", "fakename", new Date(1000000000000000000L));
		clear(pwd);
		return t;
		// TODO NOW finish method
	}
}
