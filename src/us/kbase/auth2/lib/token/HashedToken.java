package us.kbase.auth2.lib.token;

import java.util.Date;
import java.util.UUID;

public class HashedToken extends AuthToken {
	//subclassing authtoken might be a bad idea, revisit this
	
	//TODO TEST
	//TODO JAVADOC

	private UUID id;
	
	public HashedToken(
			final String tokenName,
			final UUID id,
			final String token,
			final String userName,
			final Date expirationDate) {
		super(tokenName, token, userName, expirationDate);
		if (id == null) {
			throw new NullPointerException("id");
		}
		this.id = id;
	}

	public UUID getId() {
		return id;
	}

}
