package us.kbase.auth2.lib;

public class AuthUser {

	//TODO TEST unit test
	//TODO JAVADOC
	
	private final String fullName;
	private final String email;
	private final String userName;
	
	public AuthUser(
			final String userName,
			final String email,
			final String fullName) {
		super();
		//TODO NOW check for nulls & empty strings - should email & fullName be allowed as empty strings?
		this.fullName = fullName;
		this.email = email;
		this.userName = userName;
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
}
