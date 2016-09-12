package us.kbase.auth2.lib.identity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IdentitySet {
	
	//TODO TEST
	//TODO JAVADOC
	
	private final RemoteIdentity primary;
	private final Set<RemoteIdentity> secondaries;
	
	public IdentitySet(
			final RemoteIdentity primary,
			Set<RemoteIdentity> secondaries) {
		super();
		//TODO NOW check for nulls
		this.primary = primary;
		if (secondaries == null) {
			secondaries = new HashSet<>();
		}
		this.secondaries = Collections.unmodifiableSet(secondaries);
	}

	public RemoteIdentity getPrimary() {
		return primary;
	}

	public Set<RemoteIdentity> getSecondaries() {
		return secondaries;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IdentitySet [primary=");
		builder.append(primary);
		builder.append(", secondaries=");
		builder.append(secondaries);
		builder.append("]");
		return builder.toString();
	}
}
