package us.kbase.auth2.cli;

import java.util.Arrays;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class AuthCLI {
	
	//TODO TEST
	//TODO JAVADOC
	//TODO IMPORT import users

	private static String NAME = "manageauth";
	
	public static void main(String[] args) {
		System.out.println(Arrays.asList(args));
		final Args a = new Args();
		JCommander jc = new JCommander(a);
		jc.setProgramName(NAME);
		
		try {
			jc.parse(args);
		} catch (RuntimeException e) {
			System.out.println(a);
			System.out.println("Error: " + e.getMessage());
			if (a.verbose) {
				e.printStackTrace();
			}
			System.exit(1);
		}
		if (a.help) {
			jc.usage();
		}
		System.out.println(a);
	}

	private static class Args {
		@Parameter(names = {"-h", "--help"}, help = true,
				description = "Display help.")
		private boolean help;
		
		@Parameter(names = {"-v", "--verbose"},
				description = "Show error stacktraces.")
		private boolean verbose;
		
		@Parameter(names = {"-d", "--deploy"}, required = true,
				description = "Path to the auth deploy.cfg file.")
		private String deploy;
		
		@Parameter(names = {"-r", "--set-root-password"},
				description = "Set the root user password.")
		private boolean setroot;

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Args [help=");
			builder.append(help);
			builder.append(", deploy=");
			builder.append(deploy);
			builder.append(", setroot=");
			builder.append(setroot);
			builder.append("]");
			return builder.toString();
		}
	}
}
