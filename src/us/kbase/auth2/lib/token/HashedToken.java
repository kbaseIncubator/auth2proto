package us.kbase.auth2.lib.token;

import static us.kbase.auth2.lib.Utils.checkString;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

public class HashedToken {
	//TODO TEST
	//TODO JAVADOC

	private final UUID id;
	private final String tokenName;
	private final String tokenHash;
	private final String userName;
	private final Date expirationDate;
	
	public HashedToken(
			final String tokenName,
			final UUID id,
			final String tokenHash,
			final String userName,
			final Date expirationDate) {
		checkString(tokenHash, "tokenHash", true);
		checkString(userName, "userName", true);
		if (expirationDate == null) {
			throw new IllegalArgumentException("expirationDate");
		}
		if (id == null) {
			throw new NullPointerException("id");
		}
		this.tokenName = tokenName; // null ok
		this.tokenHash = tokenHash;
		this.userName = userName;
		this.expirationDate = expirationDate;
		this.id = id;
	}

	public UUID getId() {
		return id;
	}
	
	public String getTokenName() {
		return tokenName;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public String getUserName() {
		return userName;
	}

	public Date getExpirationDate() {
		return expirationDate;
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
