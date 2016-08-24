package us.kbase.auth2.lib.token;

import static us.kbase.auth2.lib.Utils.checkString;

import java.util.Date;
import java.util.UUID;

import us.kbase.auth2.lib.UserName;

public class NewToken {

	//TODO TEST
	//TODO JAVADOC
	
	private final String tokenName;
	private final String token;
	private final UserName userName;
	private final Date expirationDate;
	private final Date creationDate = new Date();
	private final UUID id = UUID.randomUUID();
	
	public NewToken(
			final String token,
			final UserName userName,
			final Date expirationDate) {
		checkString(token, "token", true);
		if (userName == null) {
			throw new NullPointerException("userName");
		}
		if (expirationDate == null) {
			throw new NullPointerException("expirationDate");
		}
		this.tokenName = null;
		this.token = token;
		this.userName = userName;
		this.expirationDate = expirationDate;
	}
	
	public NewToken(
			final String tokenName,
			final String token,
			final UserName userName,
			final Date expirationDate) {
		checkString(token, "token", true);
		checkString(tokenName, "tokenName", true);
		if (userName == null) {
			throw new NullPointerException("userName");
		}
		if (expirationDate == null) {
			throw new NullPointerException("expirationDate");
		}
		this.tokenName = tokenName;
		this.token = token;
		this.userName = userName;
		this.expirationDate = expirationDate;
	}

	public String getTokenName() {
		return tokenName;
	}

	public UUID getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public UserName getUserName() {
		return userName;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public HashedToken getHashedToken() {
		return new HashedToken(tokenName, id, HashedToken.hash(token),
				userName, creationDate, expirationDate);
	}

}
