package us.kbase.auth2.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import us.kbase.auth2.lib.identity.RemoteIdentity;
import us.kbase.auth2.lib.token.NewToken;
import us.kbase.auth2.lib.token.TemporaryToken;

// Only one remote identity can be specified per authuser. If two remote IDs
// point to the same user choose one.
public class LoginResult {

	//TODO TEST
	//TODO JAVADOC
	private final NewToken token;
	private final TemporaryToken temporaryToken;
	private final RemoteIdentity primary;
	private final AuthUser primaryUser;
	private final Map<RemoteIdentity, AuthUser> secondaries;
	
	private LoginResult(
			final NewToken token,
			final TemporaryToken tempToken,
			final RemoteIdentity primary,
			final AuthUser primaryUser,
			final Map<RemoteIdentity, AuthUser> secondaries) {
		this.token = token;
		this.temporaryToken = tempToken;
		this.primary = primary;
		this.primaryUser = primaryUser;
		this.secondaries = Collections.unmodifiableMap(secondaries);
	}
	
	public boolean isLoggedIn() {
		return token != null;
	}
	
	public NewToken getToken() {
		return token;
	}
	
	public TemporaryToken getTemporaryToken() {
		return temporaryToken;
	}

	public RemoteIdentity getPrimaryIdentity() {
		return primary;
	}
	
	public AuthUser getPrimaryUser() {
		return primaryUser;
	}
	
	public Map<RemoteIdentity, AuthUser> getSecondaries() {
		return secondaries;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LoginResult [token=");
		builder.append(token);
		builder.append(", temporaryToken=");
		builder.append(temporaryToken);
		builder.append(", primary=");
		builder.append(primary);
		builder.append(", primaryUser=");
		builder.append(primaryUser);
		builder.append(", secondaries=");
		builder.append(secondaries);
		builder.append("]");
		return builder.toString();
	}

	public static class LoginResultBuilder {
		
		private final NewToken token;
		private final TemporaryToken temporaryToken;
		private final RemoteIdentity primary;
		private AuthUser primaryUser;
		private final Map<RemoteIdentity, AuthUser> secondaries =
				new HashMap<>();
		
		public LoginResultBuilder(final NewToken token) {
			if (token == null) {
				throw new NullPointerException("token");
			}
			this.token = token;
			this.primary = null;
			this.primaryUser = null;
			this.temporaryToken = null;
		}
		
		public LoginResultBuilder(
				final RemoteIdentity primary,
				final TemporaryToken token) {
			if (primary == null) {
				throw new NullPointerException("primary");
			}
			if (token == null) {
				throw new NullPointerException("token");
			}
			this.primary = primary;
			this.temporaryToken = token;
			this.token = null;
			this.primaryUser = null;
			
		}
		
		public LoginResultBuilder withLocalPrimary(final AuthUser user) {
			checkToken();
			if (user == null) {
				throw new NullPointerException("user");
			}
			primaryUser = user;
			return this;
		}

		private void checkToken() {
			if (token != null) {
				throw new LoginResultBuildException(
						"Cannot assign a user to the primary if user is " +
						"already logged in");
			}
		}
		
		public boolean hasSecondary(
				final RemoteIdentity remote,
				final AuthUser user) {
			return !secondaries.containsKey(remote) &&
					!secondaries.containsValue(user);
		}
		
		//assumes that which remote is associated with a user doesn't matter
		// may need changes if we need to change users
		public LoginResultBuilder withSecondary(
				final RemoteIdentity remote,
				final AuthUser user) {
			checkToken();
			if (remote == null) {
				throw new NullPointerException("remote");
			}
			if (user == null) {
				throw new NullPointerException("user");
			}
			// not expecting many values here, so O(n) is ok
			if (secondaries.containsValue(user)) {
				throw new LoginResultBuildException(
						"AuthUser already registered");
			}
			if (secondaries.containsKey(remote)) {
				throw new LoginResultBuildException(
						"RemoteIdentity already registered");
			}
			secondaries.put(remote, user);
			return this;
		}
		
		public LoginResult build() {
			return new LoginResult(
					token, temporaryToken, primary, primaryUser, secondaries);
		}
	}
	
	@SuppressWarnings("serial")
	public static class LoginResultBuildException extends RuntimeException {
		public LoginResultBuildException(final String message) {
			super(message);
		}
	}

}
