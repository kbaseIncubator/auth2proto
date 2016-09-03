package us.kbase.auth2.lib.identity;

import java.net.URL;

public class GlobusIdentityProvider implements IdentityProvider {

	public static final String NAME = "Globus";
	
	private final IdentityProviderConfig cfg;
	
	public GlobusIdentityProvider(final IdentityProviderConfig idc) {
		if (idc == null) {
			throw new NullPointerException("idc");
		}
		if (!NAME.equals(idc.getIdentityProviderName())) {
			throw new IllegalArgumentException("Bad config name: " +
					idc.getIdentityProviderName());
		}
		this.cfg = idc;
	}

	@Override
	public String getProviderName() {
		return NAME;
	}

	@Override
	public URL getLoginURI(final String state) {
		// TODO Auto-generated method stub
		return cfg.getRedirectURL(); // TODO NOW this is completely wrong
	}

}
