package us.kbase.auth2.lib;

import us.kbase.auth2.lib.exceptions.MissingParameterException;

public class UserName {

	private final String name;

	public UserName(final String name) throws MissingParameterException {
		if (name == null) {
			throw new NullPointerException("userName");
		}
		//TODO NOW appropriate checking for name - size, allowed chars, special root name
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
