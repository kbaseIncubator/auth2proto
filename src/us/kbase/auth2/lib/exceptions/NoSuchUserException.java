package us.kbase.auth2.lib.exceptions;

/** Base class of all exceptions caused by an authentication failure.
 * @author gaprice@lbl.gov 
 */
@SuppressWarnings("serial")
public class NoSuchUserException extends NoDataException {
	
	public NoSuchUserException(final String message) {
		super(AuthError.NO_SUCH_USER, message);
	}
	
	public NoSuchUserException(final String message, final Throwable cause) {
		super(AuthError.NO_SUCH_USER, message, cause);
	}
}
