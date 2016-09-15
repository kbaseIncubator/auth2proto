package us.kbase.auth2.lib;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import us.kbase.auth2.lib.identity.RemoteIdentity;
import us.kbase.auth2.lib.identity.RemoteIdentityWithID;

public class AuthUser {

	//TODO TEST unit test
	//TODO JAVADOC
	
	//an local auth user can never have identities, a regular auth user must
	// have at least one
	private final String fullName;
	private final String email;
	private final UserName userName;
	private final Set<Role> roles;
	private final Set<String> customRoles;
	private final Set<RemoteIdentityWithID> identities;
	
	public AuthUser(
			final UserName userName,
			final String email,
			final String fullName,
			Set<RemoteIdentityWithID> identities,
			Set<Role> roles,
			Set<String> customRoles) {
		super();
		//TODO NOW check for nulls & empty strings - should email & fullName be allowed as empty strings?
		this.fullName = fullName;
		this.email = email;
		this.userName = userName;
		if (identities == null) {
			identities = new HashSet<>();
		}
		this.identities = Collections.unmodifiableSet(identities);
		if (roles == null) {
			roles = new HashSet<>();
		}
		this.roles = Collections.unmodifiableSet(roles);
		if (customRoles == null) {
			customRoles = new HashSet<>();
		}
		this.customRoles = Collections.unmodifiableSet(customRoles);
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public UserName getUserName() {
		return userName;
	}

	public boolean isLocal() {
		return identities.isEmpty();
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public Set<String> getCustomRoles() {
		return customRoles;
	}
	
	public Set<RemoteIdentityWithID> getIdentities() {
		return identities;
	}
	
	public RemoteIdentityWithID getIdentity(final RemoteIdentity ri) {
		for (final RemoteIdentityWithID rid: identities) {
			if (rid.getRemoteID().equals(ri.getRemoteID())) {
				return rid;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((customRoles == null) ? 0 : customRoles.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + ((identities == null) ? 0 : identities.hashCode());
		result = prime * result + ((roles == null) ? 0 : roles.hashCode());
		result = prime * result + ((userName == null) ? 0 : userName.hashCode());
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
		AuthUser other = (AuthUser) obj;
		if (customRoles == null) {
			if (other.customRoles != null) {
				return false;
			}
		} else if (!customRoles.equals(other.customRoles)) {
			return false;
		}
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		if (fullName == null) {
			if (other.fullName != null) {
				return false;
			}
		} else if (!fullName.equals(other.fullName)) {
			return false;
		}
		if (identities == null) {
			if (other.identities != null) {
				return false;
			}
		} else if (!identities.equals(other.identities)) {
			return false;
		}
		if (roles == null) {
			if (other.roles != null) {
				return false;
			}
		} else if (!roles.equals(other.roles)) {
			return false;
		}
		if (userName == null) {
			if (other.userName != null) {
				return false;
			}
		} else if (!userName.equals(other.userName)) {
			return false;
		}
		return true;
	}
	
	
}
