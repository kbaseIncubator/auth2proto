package us.kbase.auth2.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import us.kbase.auth2.lib.identity.RemoteIdentityWithID;

public class LoginIdentities {
	
	//TODO TEST
	//TODO JAVADOC
	//TODO NOW check only one primary, check secondaries are secondary
	//TODO NOW here and in Authentication check no duplicate IDs

	private final RemoteIdentityWithID primary;
	private final AuthUser primaryUser;
	private final Map<RemoteIdentityWithID, AuthUser> secondaries;
	
	public LoginIdentities(
			final RemoteIdentityWithID primary,
			final AuthUser primaryUser,
			Map<RemoteIdentityWithID, AuthUser> secs) {
		if (primary == null) {
			throw new NullPointerException("primary");
		}
		this.primary = primary;
		this.primaryUser = primaryUser;
		if (secs == null) {
			secs = new HashMap<>();
		}
		this.secondaries = Collections.unmodifiableMap(secs);
	}

	public RemoteIdentityWithID getPrimary() {
		return primary;
	}

	public AuthUser getPrimaryUser() {
		return primaryUser;
	}

	public Map<RemoteIdentityWithID, AuthUser> getSecondaries() {
		return secondaries;
	}

}
