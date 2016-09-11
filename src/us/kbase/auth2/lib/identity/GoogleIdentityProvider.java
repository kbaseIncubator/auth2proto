package us.kbase.auth2.lib.identity;

import java.net.URI;
import java.net.URL;

public class GoogleIdentityProvider implements IdentityProvider {

	//TODO TEST
		//TODO JAVADOC
		
	
	public static final String NAME = "Google";
	
	private final IdentityProviderConfig cfg;
	
	public GoogleIdentityProvider(final IdentityProviderConfig idc) {
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
	public URI getImageURI() {
		return cfg.getImageURI();
	}

	@Override
	public URL getLoginURL(final String state) {
		// TODO Auto-generated method stub
		// TODO NOW this is completely wrong
		return cfg.getRedirectURL();
	}

	@Override
	public String getAuthCodeQueryParamName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IdentitySet getIdentities(final String authcode) {
		// TODO Auto-generated method stub
		return null;
	}

}
