package us.kbase.auth2.lib;

public class Utils {

	public static void clear(char[] pwd) {
		for (int i = 0; i < pwd.length; i++) {
			pwd[i] = '0';
		}
	}
}
