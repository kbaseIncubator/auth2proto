package us.kbase.auth2.lib;

import java.util.List;

public class LocalUser extends AuthUser {
	
	//TODO TEST unit test
	//TODO JAVADOC
	
	private final byte[] passwordHash;
	private final byte[] salt;
	private final boolean forceReset;
	
	public LocalUser(
			final UserName userName,
			final String email,
			final String fullName,
			final byte[] passwordHash,
			final byte[] salt,
			final boolean forceReset) {
		super(userName, email, fullName, true);
		//TODO NOW check for nulls & empty strings - should email & fullName be allowed as empty strings?
		this.passwordHash = passwordHash;
		this.salt = salt;
		this.forceReset = forceReset;
	}
	
	public LocalUser(
			final UserName userName,
			final String email,
			final String fullName,
			final List<String> roles,
			final List<String> customRoles,
			final byte[] passwordHash,
			final byte[] salt,
			final boolean forceReset) {
		super(userName, email, fullName, true, roles, customRoles);
		//TODO NOW check for nulls & empty strings - should email & fullName be allowed as empty strings?
		this.passwordHash = passwordHash;
		this.salt = salt;
		this.forceReset = forceReset;
	}


	public byte[] getPasswordHash() {
		return passwordHash;
	}

	public byte[] getSalt() {
		return salt;
	}

	public boolean forceReset() {
		return forceReset;
	}
}
