package us.kbase.auth2.lib.storage;

import us.kbase.auth2.lib.identity.RemoteIdentity;

public class TemporaryStoredIdentity {

	//TODO TEST
	//TODO JAVADOC
	
	private final String provider;
	private final String id;
	private final boolean primary;
	
	public TemporaryStoredIdentity(
			final String provider,
			final String id,
			final boolean primary) {
		super();
		//TODO NOW check inputs
		this.provider = provider;
		this.id = id;
		this.primary = primary;
		
	}
	
	public TemporaryStoredIdentity(
			final RemoteIdentity id,
			final boolean primary) {
		//TODO NOW check inputs
		if (id == null) {
			throw new NullPointerException("id");
		}
		this.provider = id.getProvider();
		this.id = id.getId();
		this.primary = primary;
	}

	public String getProvider() {
		return provider;
	}

	public String getId() {
		return id;
	}

	public boolean isPrimary() {
		return primary;
	}
}
