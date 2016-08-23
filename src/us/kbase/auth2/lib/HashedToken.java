package us.kbase.auth2.lib;

import java.util.Date;

public class HashedToken extends AuthToken {
	//subclassing authtoken might be a bad idea, revisit this

	HashedToken(
			final String token,
			final String userName,
			final Date expirationDate) {
		super(token, userName, expirationDate);
	}

}
