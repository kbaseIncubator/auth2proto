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
	private final List<String> roles;
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
		this.roles = Collections.unmodifiableList(new LinkedList<String>());
		this.customRoles = Collections.unmodifiableList(
				new LinkedList<String>());
	}
	
	public AuthUser(
			final UserName userName,
			final String email,
			final String fullName,
			final boolean isLocal,
			List<String> roles,
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

	public List<String> getRoles() {
		return roles;
	}

	public List<String> getCustomRoles() {
		return customRoles;
	}	
}
