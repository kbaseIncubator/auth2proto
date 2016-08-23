package us.kbase.auth2.service.exceptions;

public enum AuthError {
	
	//TODO TEST unit tests
	//TODO JAVADOC
	
	AUTHENICATION_FAILED	(10000, "Authentication failed"),
	UNAUTHORIZED			(20000, "Unauthorized"),
	MISSING_PARAMETER		(30000, "Missing input parameter"),
	USER_ALREADY_EXISTS		(30001, "User already exists");
	
	private final int errcode;
	private final String error;
	
	AuthError(final int errcode, final String error) {
		this.errcode = errcode;
		this.error = error;
	}

	public int getErrorCode() {
		return errcode;
	}

	public String getError() {
		return error;
	}

}
