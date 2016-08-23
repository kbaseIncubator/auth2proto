package us.kbase.auth2.lib;

public class LocalUser {
	
	//TODO TEST unit test
	//TODO JAVADOC
	
	private final String fullName;
	private final String email;
	private final String userName;
	private final byte[] passwordHash;
	private final byte[] salt;
	private final boolean forceReset;
	
	public LocalUser(
			final String userName,
			final String email,
			final String fullName,
			final byte[] passwordHash,
			final byte[] salt,
			final boolean forceReset) {
		super();
		//TODO NOW check for nulls & empty strings - should email & fullName be allowed as empty strings?
		this.fullName = fullName;
		this.email = email;
		this.userName = userName;
		this.passwordHash = passwordHash;
		this.salt = salt;
		this.forceReset = forceReset;
	}

	public String getFullName() {
		return fullName;
	}

	public String getEmail() {
		return email;
	}

	public String getUserName() {
		return userName;
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
