package us.kbase.auth2.lib.identity;

public class RemoteIdentity {
	
	//TODO JAVADOC
	//TODO TEST
	
	private final String provider;
	private final String id;
	private final String username;
	private final String fullname;
	private final String email;
	
	public RemoteIdentity(
			final String provider,
			final String id,
			final String username,
			final String fullname,
			final String email) {
		super();
		//TODO NOW check for null & .trim().isEmpty()
		this.provider = provider;
		this.id = id;
		this.username = username;
		this.fullname = fullname;
		this.email = email;
	}

	public String getProvider() {
		return provider;
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getFullname() {
		return fullname;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RemoteIdentity [provider=");
		builder.append(provider);
		builder.append(", id=");
		builder.append(id);
		builder.append(", username=");
		builder.append(username);
		builder.append(", fullname=");
		builder.append(fullname);
		builder.append(", email=");
		builder.append(email);
		builder.append("]");
		return builder.toString();
	}

}
