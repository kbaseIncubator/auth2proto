package us.kbase.auth2.lib.token;

import static us.kbase.auth2.lib.Utils.checkString;

import java.util.Date;
import java.util.UUID;

public class NewToken {

	//TODO TEST
	//TODO JAVADOC
	//TODO NOW change to NewToken
	
	private final String tokenName;
	private final String token;
	private final String userName;
	private final Date expirationDate;
	
	public NewToken(
			final String token,
			final String userName,
			final Date expirationDate) {
		checkString(token, "token", true);
		checkString(userName, "userName", true);
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
			final String userName,
			final Date expirationDate) {
		checkString(token, "token", true);
		checkString(userName, "userName", true);
		if (expirationDate == null) {
			throw new NullPointerException("expirationDate");
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
		return new HashedToken(tokenName, UUID.randomUUID(),
				HashedToken.hash(token), userName, expirationDate);
	}

}
