package us.kbase.auth2.service.exceptions.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ErrorMessage {
	
	//TODO TEST unit tests
	//TODO JAVADOC

	private final int httpCode;
	private final String httpStatus;
	private final Integer appCode;
	private final String appErr;
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
		//TODO NOW handle AuthException and subclasses
		if (ex instanceof WebApplicationException) {
			final Response res = ((WebApplicationException) ex).getResponse();
			appCode = null;
			appErr = null;
			httpCode = res.getStatus();
			httpStatus = res.getStatusInfo().getReasonPhrase();
			message = ex.getMessage();
		} else {
			//defaults to internal server error 500
			appCode = null;
			appErr = null;
			httpCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
			httpStatus = Response.Status.INTERNAL_SERVER_ERROR
					.getReasonPhrase();
			message = ex.getMessage();
		}
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

	public String getAppErr() {
		return appErr;
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
		builder.append(", appErr=");
		builder.append(appErr);
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
