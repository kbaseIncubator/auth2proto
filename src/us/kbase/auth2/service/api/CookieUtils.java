package us.kbase.auth2.service.api;

import java.util.Date;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import us.kbase.auth2.lib.token.NewToken;

public class CookieUtils {

	public static NewCookie getCookie(
			final NewToken t,
			final boolean session) {
		return new NewCookie(new Cookie("token", t.getToken(), "/", null),
				"authtoken", getMaxAge(t, session), false);
		//TODO CONFIG make secure cookie configurable
	}
	
	private static int getMaxAge(final NewToken t, final boolean session) {
		if (session) {
			return NewCookie.DEFAULT_MAX_AGE;
		}
		final long exp = (long) Math.floor((
				t.getExpirationDate().getTime() - new Date().getTime()) /
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
