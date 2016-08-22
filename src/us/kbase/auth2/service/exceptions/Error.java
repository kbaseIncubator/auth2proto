package us.kbase.auth2.service.exceptions;

public enum Error {
	
	//TODO TEST unit tests
	//TODO JAVADOC
	
	AUTHENICATION_FAILED	(10000, "Authentication failed"),
	UNAUTHORIZED			(20000, "Unauthorized"),
	BAD_INPUT				(30000, "Bad Input"); //this should go
	
	private final int errcode;
	private final String error;
	
	Error(final int errcode, final String error) {
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
