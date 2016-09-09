package us.kbase.auth2.lib.token;

import static us.kbase.auth2.lib.Utils.checkString;

import java.util.Date;
import java.util.UUID;

public class TemporaryToken {
	
	//TODO TEST
	//TODO JAVADOC
	
	private final String token;
	private final Date created = new Date();
	private final Date expiry;
	private final UUID id = UUID.randomUUID();

	public TemporaryToken(final String token, final Date expiry) {
		checkString(token, "token", true);
		if (expiry == null) {
			throw new NullPointerException("expiry");
		}
		this.expiry = expiry;
		this.token = token;
	}

	public String getToken() {
		return token;
	}
	
	public Date getCreationDate() {
		return created;
	}

	public Date getExpirationDate() {
		return expiry;
	}

	public UUID getId() {
		return id;
	}

	public TemporaryHashedToken getHashedToken() {
		return new TemporaryHashedToken(
				HashedToken.hash(token), id, created, expiry);
	}
}
