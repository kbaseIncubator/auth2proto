package us.kbase.auth2.service.api;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.LoginResult;
import us.kbase.auth2.lib.identity.RemoteIdentity;

public class LoginResultTransfer {

	//TODO TEST
	//TODO JAVADOC
	
	public final String provider;
	public final LoginTransferID primary;
	public final List<LoginTransferID> secondaries;
	
	public LoginResultTransfer(final LoginResult lr) {
		provider = lr.getPrimaryIdentity().getProvider();
		primary = new LoginTransferID(
				lr.getPrimaryIdentity(), lr.getPrimaryUser());
		
		secondaries = new LinkedList<>();
		for (final Entry<RemoteIdentity, AuthUser> e:
				lr.getSecondaries().entrySet()) {
			secondaries.add(new LoginTransferID(e.getKey(), e.getValue()));
		}
	}
	
	public class LoginTransferID {
		
		public final String remoteID;
		public final String remoteUserName;
		public final String localUserName;
		
		public LoginTransferID(final RemoteIdentity id, final AuthUser u) {
			remoteID = id.getId();
			remoteUserName = id.getUsername();
			localUserName = u == null ? null : u.getUserName().getName();
		}
	}
}
