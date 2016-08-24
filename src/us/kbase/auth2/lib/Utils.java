package us.kbase.auth2.lib;

import us.kbase.auth2.lib.exceptions.MissingParameterException;

public class Utils {

	public static void clear(char[] pwd) {
		for (int i = 0; i < pwd.length; i++) {
			pwd[i] = '0';
		}
	}
	
	public static void checkString(final String s, final String name)
			throws MissingParameterException {
		if (s == null || s.isEmpty()) {
			throw new MissingParameterException("Missing parameter: " + name);
		}
	}
	
	public static void checkString(
			final String s,
			final String name,
			final boolean argexcept) {
		if (s == null || s.isEmpty()) {
			throw new IllegalArgumentException("Missing argument: " + name);
		}
	}
}
