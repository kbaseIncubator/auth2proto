package us.kbase.auth2.lib.exceptions;

/** A required parameter was not provided.
 * @author gaprice@lbl.gov
 *
 */
@SuppressWarnings("serial")
public class MissingParameterException extends AuthException {

	public MissingParameterException(String message) {
		super(AuthError.MISSING_PARAMETER, message);
	}

	public MissingParameterException(String message, Throwable cause) {
		super(AuthError.MISSING_PARAMETER, message, cause);
	}
}
