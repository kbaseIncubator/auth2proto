package us.kbase.auth2.service.exceptions;

/** Base class of all authorization service exceptions.
 * @author gaprice@lbl.gov 
 */
@SuppressWarnings("serial")
public class AuthException extends Exception {
	
	private final Error err;
	
	public AuthException(final Error err, final String message) {
		super(message);
		if (err == null) {
			throw new NullPointerException("err");
		}
		this.err = err;
	}
	
	public AuthException(
			final Error err,
			final String message,
			final Throwable cause) {
		super(message, cause);
		if (err == null) {
			throw new NullPointerException("err");
		}
		this.err = err;
	}

	public Error getErr() {
		return err;
	}
}
