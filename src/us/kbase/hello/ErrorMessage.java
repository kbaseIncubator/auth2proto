package us.kbase.hello;

public class ErrorMessage {

	private final int code;
	private final String status;
	private final String message;
	private final String exception;

	public ErrorMessage(
			final int code,
			final String status,
			final String message,
			final String exception) {
		super();
		this.code = code;
		this.status = status;
		this.message = message;
		this.exception = exception;
	}

	public int getCode() {
		return code;
	}

	public String getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public String getException() {
		return exception;
	}

}
