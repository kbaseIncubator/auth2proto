package us.kbase.auth2.lib;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class AuthUser {

	//TODO TEST unit test
	//TODO JAVADOC
	
	private final String fullName;
	private final String email;
	private final UserName userName;
	private final boolean isLocal;
	private final List<Role> roles;
	private final List<String> customRoles;
	
	public AuthUser(
			final UserName userName,
			final String email,
			final String fullName,
			final boolean isLocal) {
		super();
		//TODO NOW check for nulls & empty strings - should email & fullName be allowed as empty strings?
		this.fullName = fullName;
		this.email = email;
		this.userName = userName;
		this.isLocal = isLocal;
		this.roles = Collections.unmodifiableList(new LinkedList<Role>());
		this.customRoles = Collections.unmodifiableList(
				new LinkedList<String>());
	}
	
	public AuthUser(
			final UserName userName,
			final String email,
			final String fullName,
			final boolean isLocal,
			List<Role> roles,
			List<String> customRoles) {
		super();
		//TODO NOW check for nulls & empty strings - should email & fullName be allowed as empty strings?
		this.fullName = fullName;
		this.email = email;
		this.userName = userName;
		this.isLocal = isLocal;
		if (roles == null) {
			roles = new LinkedList<>();
		}
		this.roles = Collections.unmodifiableList(roles);
		if (customRoles == null) {
			customRoles = new LinkedList<>();
		}
		this.customRoles = Collections.unmodifiableList(customRoles);
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
		return isLocal;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public List<String> getCustomRoles() {
		return customRoles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((customRoles == null) ? 0 : customRoles.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + (isLocal ? 1231 : 1237);
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
		if (isLocal != other.isLocal) {
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
