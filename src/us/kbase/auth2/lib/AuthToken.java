package us.kbase.auth2.lib;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

public class AuthToken {

	private final String token;
	private final String userName;
	private final Date expirationDate;
	
	public AuthToken(
			final String token,
			final String userName,
			final Date expirationDate) {
		if (token == null || userName == null || expirationDate == null) {
			throw new IllegalArgumentException("no null args");
		}
		this.token = token;
		this.userName = userName;
		this.expirationDate = expirationDate;
	}

	public String getToken() {
		return token;
	}

	public String getUserName() {
		return userName;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public HashedToken getHashedToken() {
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("This should be impossible", e);
		}
		final byte[] hash = digest.digest(
				token.getBytes(StandardCharsets.UTF_8));
		final String b64hash = Base64.getEncoder().encodeToString(hash);
		return new HashedToken(b64hash, userName, expirationDate);
	}

}
