package us.kbase.auth2.lib.exceptions;

/** Base class of all exceptions caused by an authentication failure.
 * @author gaprice@lbl.gov 
 */
@SuppressWarnings("serial")
public class UserExistsException extends AuthException {
	
	public UserExistsException(final String message) {
		super(AuthError.USER_ALREADY_EXISTS, message);
	}
	
	public UserExistsException(final String message, final Throwable cause) {
		super(AuthError.USER_ALREADY_EXISTS, message, cause);
	}
}
