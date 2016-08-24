package us.kbase.auth2.lib.token;

import static us.kbase.auth2.lib.Utils.checkString;

public class IncomingHashedToken {
	
	//TODO TEST
	//TODO JAVADOC

	private final String token;

	public IncomingHashedToken(String token) {
		super();
		checkString(token, "token", true);
		this.token = token;
	}

	public String getToken() {
		return token;
	}

}
