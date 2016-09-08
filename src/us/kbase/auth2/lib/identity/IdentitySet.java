package us.kbase.auth2.lib.identity;

import java.util.Collections;
import java.util.List;

public class IdentitySet {
	
	//TODO TEST
	//TODO JAVADOC
	
	private final RemoteIdentity primary;
	private final List<RemoteIdentity> secondaries;
	
	public IdentitySet(
			final RemoteIdentity primary,
			final List<RemoteIdentity> secondaries) {
		super();
		//TODO NOW check for nulls
		this.primary = primary;
		this.secondaries = Collections.unmodifiableList(secondaries);
	}

	public RemoteIdentity getPrimary() {
		return primary;
	}

	public List<RemoteIdentity> getSecondaries() {
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
