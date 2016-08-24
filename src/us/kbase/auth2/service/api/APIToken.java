package us.kbase.auth2.service.api;

import us.kbase.auth2.lib.token.HashedToken;

public class APIToken {
	
	//TODO TEST
	//TODO JAVADOC
	
	private final String id;
	private final long expires;
	private final long created;
	private final String name;
	private final String user;

	public APIToken(final HashedToken token) {
		if (token == null) {
			throw new NullPointerException("token");
		}
		this.id = token.getId().toString();
		this.name = token.getTokenName();
		this.user = token.getUserName().getName();
		this.expires = (long) Math.floor(
				token.getExpirationDate().getTime() / 1000.0);
		this.created = (long) Math.floor(
				token.getCreationDate().getTime() / 1000.0);
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

	public String getName() {
		return name;
	}

	public String getUser() {
		return user;
	}

}
