package us.kbase.auth2.lib;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import us.kbase.auth2.lib.identity.RemoteIdentity;

public class LinkIdentities {
	
	//TODO TEST
	//TODO JAVADOC
	
	private final AuthUser user;
	private final Set<RemoteIdentity> idents;

	public LinkIdentities(
			final AuthUser user,
			Set<RemoteIdentity> identities) {
		if (user == null) {
			throw new NullPointerException("user");
		}
		if (identities == null) {
			identities = new HashSet<>();
		}
		this.user = user;
		this.idents = Collections.unmodifiableSet(identities);
	}

	public AuthUser getUser() {
		return user;
	}

	public Set<RemoteIdentity> getIdentities() {
		return idents;
	}

}
