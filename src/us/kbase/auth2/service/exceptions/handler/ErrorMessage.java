package us.kbase.auth2.service.exceptions.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import us.kbase.auth2.service.exceptions.AuthException;
import us.kbase.auth2.service.exceptions.AuthenticationException;
import us.kbase.auth2.service.exceptions.UnauthorizedException;

@JsonInclude(Include.NON_NULL)
public class ErrorMessage {
	
	//TODO TEST unit tests
	//TODO JAVADOC

	private final int httpCode;
	private final String httpStatus;
	private final Integer appCode;
	private final String appError;
	private final String message;
	private final String exception;
	@JsonIgnore
	private final List<String> exceptionLines;
	@JsonIgnore
	private final boolean hasException;
	
	public ErrorMessage(final Throwable ex, final boolean includeTrace) {
		if (ex == null) {
			throw new NullPointerException("exp");
		}
		if (includeTrace) {
			final StringWriter st = new StringWriter();
			ex.printStackTrace(new PrintWriter(st));
			exception = st.toString();
			exceptionLines = Collections.unmodifiableList(
					Arrays.asList(exception.split("\n")));
			hasException = true;
		} else {
			exception = null;
			exceptionLines = null;
			hasException = false;
		}
		message = ex.getMessage();
		final StatusType status;
		if (ex instanceof AuthException) {
			final AuthException ae = (AuthException) ex;
			appCode = ae.getErr().getErrorCode();
			appError = ae.getErr().getError();
			if (ae instanceof AuthenticationException) {
				status = Response.Status.UNAUTHORIZED;
			} else if (ae instanceof UnauthorizedException) {
				status = Response.Status.FORBIDDEN;
			} else {
				status = Response.Status.BAD_REQUEST;
			}
		} else if (ex instanceof WebApplicationException) {
			appCode = null;
			appError = null;
			status = ((WebApplicationException) ex).getResponse()
					.getStatusInfo();
		} else {
			appCode = null;
			appError = null;
			status = Response.Status.INTERNAL_SERVER_ERROR;
		}
		httpCode = status.getStatusCode();
		httpStatus = status.getReasonPhrase();
	}

	public int getHttpCode() {
		return httpCode;
	}

	public String getHttpStatus() {
		return httpStatus;
	}

	public Integer getAppCode() {
		return appCode;
	}

	public String getAppError() {
		return appError;
	}

	public String getMessage() {
		return message;
	}

	public String getException() {
		return exception;
	}

	public List<String> getExceptionLines() {
		return exceptionLines;
	}

	public boolean hasException() {
		return hasException;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ErrorMessage [httpCode=");
		builder.append(httpCode);
		builder.append(", httpStatus=");
		builder.append(httpStatus);
		builder.append(", appCode=");
		builder.append(appCode);
		builder.append(", appError=");
		builder.append(appError);
		builder.append(", message=");
		builder.append(message);
		builder.append(", exception=");
		builder.append(exception);
		builder.append(", exceptionLines=");
		builder.append(exceptionLines);
		builder.append(", hasException=");
		builder.append(hasException);
		builder.append("]");
		return builder.toString();
	}
}
