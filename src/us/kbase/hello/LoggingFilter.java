package us.kbase.hello;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;

import org.slf4j.LoggerFactory;

import us.kbase.common.service.JsonServerSyslog;
import us.kbase.common.service.JsonServerSyslog.RpcInfo;

// NOTE must be in us.kbase package for the JsonServerSyslog to work

public class LoggingFilter implements ContainerRequestFilter,
		ContainerResponseFilter {
	
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private static final String X_REAL_IP = "X-Real-IP";
	private static final String USER_AGENT = "User-Agent";
	
	@Context
	private HttpServletRequest servletRequest;
	
	@Override
	public void filter(final ContainerRequestContext reqcon)
			throws IOException {
		final RpcInfo rpc = JsonServerSyslog.getCurrentRpcInfo();
		rpc.setId(("" + Math.random()).substring(2));
		//TODO AUTH get config and set ignoreIPs appropriately
		rpc.setIp(getIpAddress(reqcon, false));
		rpc.setMethod(reqcon.getMethod());
	}
	
	//TODO AUTH TEST
	public String getIpAddress(
			final ContainerRequestContext request,
			final boolean ignoreIPsInHeaders) {
		final String xFF = request.getHeaderString(X_FORWARDED_FOR);
		final String realIP = request.getHeaderString(X_REAL_IP);

		if (!ignoreIPsInHeaders) {
			if (xFF != null && !xFF.isEmpty()) {
				return xFF.split(",")[0].trim();
			}
			if (realIP != null && !realIP.isEmpty()) {
				return realIP.trim();
			}
		}
		return servletRequest.getRemoteAddr();
	}

	@Override
	public void filter(
			final ContainerRequestContext reqcon,
			final ContainerResponseContext rescon)
			throws IOException {
		LoggerFactory.getLogger(getClass()).info("{} {} {}",
				reqcon.getUriInfo().getAbsolutePath(),
				rescon.getStatus(),
				reqcon.getHeaderString(USER_AGENT));
	}

}
