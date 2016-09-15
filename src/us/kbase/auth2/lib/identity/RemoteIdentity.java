package us.kbase.auth2.lib.identity;

import java.util.UUID;

public class RemoteIdentity {
	
	//TODO JAVADOC
	//TODO TEST
	
	private final RemoteIdentityID remoteID;
	private final String username;
	private final String fullname;
	private final String email;
	private final boolean primary;
	
	public RemoteIdentity(
			final RemoteIdentityID remoteID,
			final String username,
			final String fullname,
			final String email,
			final boolean primary) {
		super();
		//TODO NOW check for null & .trim().isEmpty()
		if (remoteID == null) {
			throw new NullPointerException("id");
		}
		this.remoteID = remoteID;
		this.username = username;
		this.fullname = fullname;
		this.email = email;
		this.primary = primary;
	}
	
	public RemoteIdentityWithID withID() {
		return withID(UUID.randomUUID());
	}
	
	public RemoteIdentityWithID withID(final UUID id) {
		return new RemoteIdentityWithID(id, this.remoteID,
				username, fullname, email, primary);
	}

	public RemoteIdentityID getRemoteID() {
		return remoteID;
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
	
	public boolean isEqualProviderDetails(final RemoteIdentity other) {
		return remoteID.equals(other.remoteID) &&
				email.equals(other.email) &&
				fullname.equals(other.fullname) &&
				username.equals(other.username) &&
				primary == other.primary;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
		result = prime * result + ((remoteID == null) ? 0 : remoteID.hashCode());
		result = prime * result + (primary ? 1231 : 1237);
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
		if (remoteID == null) {
			if (other.remoteID != null) {
				return false;
			}
		} else if (!remoteID.equals(other.remoteID)) {
			return false;
		}
		if (primary != other.primary) {
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
		builder.append("RemoteIdentity [remoteID=");
		builder.append(remoteID);
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
