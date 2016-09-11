package us.kbase.auth2.service.api;

import java.util.Date;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import us.kbase.auth2.lib.token.NewToken;
import us.kbase.auth2.lib.token.TemporaryToken;

public class CookieUtils {

	public static NewCookie getLoginCookie(final NewToken token) {
		return getLoginCookie(token, false);
	}
	
	public static NewCookie getLoginCookie(
			final NewToken token,
			final boolean session) {
		return new NewCookie(new Cookie("token",
				token == null ? "no token" :token.getToken(), "/", null),
				"authtoken",
				token == null ? 0 :getMaxCookieAge(token, session),
				APIConstants.SECURE_COOKIES);
	}
	
	public static int getMaxCookieAge(
			final NewToken token,
			final boolean session) {
		return getMaxCookieAge(token.getExpirationDate(), session);
	}
	
	public static int getMaxCookieAge(
			final TemporaryToken token,
			final boolean session) {
		return getMaxCookieAge(token.getExpirationDate(), session);
	}
	
	private static int getMaxCookieAge(
			final Date expiration,
			final boolean session) {
	
		if (session) {
			return NewCookie.DEFAULT_MAX_AGE;
		}
		final long exp = (long) Math.floor((
				expiration.getTime() - new Date().getTime()) /
				1000.0);
		if (exp > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if (exp < 0) {
			return 0;
		}
		return (int) exp;
	}
}
