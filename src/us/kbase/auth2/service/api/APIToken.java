package us.kbase.auth2.service.api;

import static us.kbase.auth2.lib.Utils.dateToSec;

import java.util.Date;
import java.util.UUID;

import us.kbase.auth2.lib.UserName;
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
		this(token.getTokenName(), token.getId(), token.getUserName(),
				token.getCreationDate(), token.getExpirationDate());
	}

	APIToken(
			final String tokenName,
			final UUID id,
			final UserName userName,
			final Date creationDate,
			final Date expirationDate) {
		this.id = id.toString();
		this.name = tokenName;
		this.user = userName.getName();
		this.expires = dateToSec(expirationDate);
		this.created = dateToSec(creationDate);
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
