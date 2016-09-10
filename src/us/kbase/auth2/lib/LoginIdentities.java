package us.kbase.auth2.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import us.kbase.auth2.lib.identity.RemoteIdentity;

public class LoginIdentities {
	
	//TODO NOW TEST
	//TODO NOW JAVADOC
	//TODO NOW check only one primary, check secondaries are secondary
	//TODO NOW here and in Authentication check no duplicate IDs

	private final RemoteIdentity primary;
	private final AuthUser primaryUser;
	private final Map<RemoteIdentity, AuthUser> secondaries;
	
	public LoginIdentities(
			final RemoteIdentity primary,
			final AuthUser primaryUser,
			Map<RemoteIdentity, AuthUser> secondaries) {
		if (primary == null) {
			throw new NullPointerException("primary");
		}
		this.primary = primary;
		this.primaryUser = primaryUser;
		if (secondaries == null) {
			secondaries = new HashMap<>();
		}
		this.secondaries = Collections.unmodifiableMap(secondaries);
	}

	public RemoteIdentity getPrimary() {
		return primary;
	}

	public AuthUser getPrimaryUser() {
		return primaryUser;
	}

	public Map<RemoteIdentity, AuthUser> getSecondaries() {
		return secondaries;
	}

}
