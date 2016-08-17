package hellotest;

public class HelloError {

	private final int status;
	private final String message;
	
	public HelloError(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

}
