package us.kbase.auth2.lib.token;

import static us.kbase.auth2.lib.Utils.checkString;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class AuthToken {

	//TODO TEST
	//TODO JAVADOC
	
	private final String tokenName;
	private final String token;
	private final String userName;
	private final Date expirationDate;
	
	public AuthToken(
			final String tokenName,
			final String token,
			final String userName,
			final Date expirationDate) {
		checkString(token, "token", true);
		checkString(userName, "userName", true);
		if (expirationDate == null) {
			throw new IllegalArgumentException("expirationDate");
		}
		this.tokenName = tokenName; // null ok
		this.token = token;
		this.userName = userName;
		this.expirationDate = expirationDate;
	}

	public String getTokenName() {
		return tokenName;
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
		return new HashedToken(tokenName, UUID.randomUUID(), hash(token),
				userName, expirationDate);
	}

	public static String hash(final String token) {
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("This should be impossible", e);
		}
		final byte[] hash = digest.digest(
				token.getBytes(StandardCharsets.UTF_8));
		final String b64hash = Base64.getEncoder().encodeToString(hash);
		return b64hash;
	}

}
