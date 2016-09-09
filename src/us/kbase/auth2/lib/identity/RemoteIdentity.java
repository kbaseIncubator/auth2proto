package us.kbase.auth2.lib.identity;

public class RemoteIdentity {
	
	//TODO JAVADOC
	//TODO TEST
	
	private final String provider;
	private final String id;
	private final String username;
	private final String fullname;
	private final String email;
	private final boolean primary;
	
	public RemoteIdentity(
			final String provider,
			final String id,
			final String username,
			final String fullname,
			final String email,
			final boolean primary) {
		super();
		//TODO NOW check for null & .trim().isEmpty()
		this.provider = provider;
		this.id = id;
		this.username = username;
		this.fullname = fullname;
		this.email = email;
		this.primary = primary;
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

	public boolean isPrimary() {
		return primary;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (primary ? 1231 : 1237);
		result = prime * result + ((provider == null) ? 0 : provider.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RemoteIdentity other = (RemoteIdentity) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		if (fullname == null) {
			if (other.fullname != null) {
				return false;
			}
		} else if (!fullname.equals(other.fullname)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (primary != other.primary) {
			return false;
		}
		if (provider == null) {
			if (other.provider != null) {
				return false;
			}
		} else if (!provider.equals(other.provider)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
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
		builder.append(", primary=");
		builder.append(primary);
		builder.append("]");
		return builder.toString();
	}

}
