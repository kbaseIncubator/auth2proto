package us.kbase.auth2.service.exceptions;

/** Base class of all exceptions caused by an authorization failure.
 * @author gaprice@lbl.gov 
 */
@SuppressWarnings("serial")
public class UnauthorizedException extends AuthException {
	
	
	public UnauthorizedException(final Error err, final String message) {
		super(err, message);
	}
	
	public UnauthorizedException(
			final Error err,
			final String message,
			final Throwable cause) {
		super(err, message, cause);
	}
}
