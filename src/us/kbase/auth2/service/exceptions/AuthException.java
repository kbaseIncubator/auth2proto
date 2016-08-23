package us.kbase.auth2.service.exceptions;

/** Base class of all authorization service exceptions.
 * @author gaprice@lbl.gov 
 */
@SuppressWarnings("serial")
public class AuthException extends Exception {
	
	private final AuthError err;
	
	public AuthException(final AuthError err, final String message) {
		super(err.getErrorCode() + " " + err.getError() + ": " + message);
		this.err = err;
	}
	
	public AuthException(
			final AuthError err,
			final String message,
			final Throwable cause) {
		super(err.getErrorCode() + " " + err.getError() + ": " + message,
				cause);
		this.err = err;
	}

	public AuthError getErr() {
		return err;
	}
}
