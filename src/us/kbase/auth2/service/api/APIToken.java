package us.kbase.auth2.service.api;

import us.kbase.auth2.lib.token.HashedToken;

public class APIToken {
	
	//TODO TEST
	//TODO JAVADOC
	
	private final String id;
	private final long expiration;
	private final String tokenName;
	private final String userName;

	public APIToken(final HashedToken token) {
		if (token == null) {
			throw new NullPointerException("token");
		}
		this.id = token.getId().toString();
		this.tokenName = token.getTokenName();
		this.userName = token.getUserName();
		this.expiration = (long) Math.floor(
				token.getExpirationDate().getTime() / 1000.0);
	}

	public String getId() {
		return id;
	}

	public long getExpiration() {
		return expiration;
	}

	public String getTokenName() {
		return tokenName;
	}

	public String getUserName() {
		return userName;
	}

}
