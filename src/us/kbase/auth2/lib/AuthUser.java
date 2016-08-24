package us.kbase.auth2.lib;

public class AuthUser {

	//TODO TEST unit test
	//TODO JAVADOC
	
	private final String fullName;
	private final String email;
	private final UserName userName;
	
	public AuthUser(
			final UserName userName,
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

	public UserName getUserName() {
		return userName;
	}	
}
