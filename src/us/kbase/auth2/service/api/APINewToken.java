package us.kbase.auth2.service.api;

import us.kbase.auth2.lib.token.NewToken;

public class APINewToken {

	private final String name;
	private final String user;
	private final String token;
	private final String id;
	private final long created;
	private final long expires;
	
	//TODO TEST
	//TODO JAVADOC
	
	//TODO NOW consider inheriting from APIToken, would need a new constructor (or hash the token again)
	
	public APINewToken(final NewToken token) {
		if (token == null) {
			throw new NullPointerException("t");
		}
		this.name = token.getTokenName();
		this.user = token.getUserName().getName();
		this.token = token.getToken();
		this.id = token.getId().toString();
		this.expires = (long) Math.floor(
				token.getExpirationDate().getTime() / 1000.0);
		this.created = (long) Math.floor(
				token.getCreationDate().getTime() / 1000.0);
	}

	public String getName() {
		return name;
	}

	public String getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}

	public String getId() {
		return id;
	}

	public long getCreated() {
		return created;
	}

	public long getExpires() {
		return expires;
	}

}
