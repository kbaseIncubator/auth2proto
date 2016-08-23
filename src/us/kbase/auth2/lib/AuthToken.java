package us.kbase.auth2.lib;

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

}
