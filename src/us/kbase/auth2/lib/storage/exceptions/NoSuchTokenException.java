package us.kbase.auth2.lib.storage.exceptions;

public class NoSuchTokenException extends AuthStorageException {

	private static final long serialVersionUID = 1L;
	
	public NoSuchTokenException(String message) { super(message); }
	public NoSuchTokenException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
