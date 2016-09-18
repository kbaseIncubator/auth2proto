package us.kbase.auth2.lib;

import java.util.UUID;

import us.kbase.auth2.lib.exceptions.MissingParameterException;

public class CustomRole {
	
	private final String name;
	private final String desc;
	private final UUID id;
	
	//TODO NOW make the name the ID and store that in the DB. The name should be sanity checked for length and url safety. Note that the id is permanent.
	//TODO LATER remote role from all users fucntion
	
	public CustomRole(final UUID id, final String name, final String desc)
			throws MissingParameterException {
		super();
		if (name == null || name.isEmpty()) {
			throw new MissingParameterException("name");
		}
		if (desc == null || desc.isEmpty()) {
			throw new MissingParameterException("desc");
		}
		if (id == null) {
			throw new NullPointerException("id");
		}
		this.name = name;
		this.desc = desc;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public UUID getId() {
		return id;
	}
}
