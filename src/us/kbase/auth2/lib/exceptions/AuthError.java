package us.kbase.auth2.lib.exceptions;

public enum AuthError {
	
	//TODO TEST unit tests
	//TODO JAVADOC
	
	AUTHENICATION_FAILED	(10000, "Authentication failed"),
	NO_TOKEN				(10001, "No authentication token"),
	INVALID_TOKEN			(10002, "Invalid token"),
	UNAUTHORIZED			(20000, "Unauthorized"),
	MISSING_PARAMETER		(30000, "Missing input parameter"),
	USER_ALREADY_EXISTS		(30001, "User already exists"),
	NO_SUCH_USER			(30002, "No such user"),
	NO_SUCH_TOKEN			(30003, "No such token"),
	UNSUPPORTED_OP			(40001, "Unsupported opertation");
	
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
