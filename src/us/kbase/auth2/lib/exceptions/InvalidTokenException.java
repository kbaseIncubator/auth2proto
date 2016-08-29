package us.kbase.auth2.lib.exceptions;

/** Base class of all exceptions caused by an authentication failure.
 * @author gaprice@lbl.gov 
 */
@SuppressWarnings("serial")
public class InvalidTokenException extends AuthenticationException {
	
	public InvalidTokenException() {
		super(AuthError.INVALID_TOKEN, null);
	}
	
	public InvalidTokenException(final String message) {
		super(AuthError.INVALID_TOKEN, message);
	}
	
	public InvalidTokenException(final String message, final Throwable cause) {
		super(AuthError.INVALID_TOKEN, message, cause);
	}
}
