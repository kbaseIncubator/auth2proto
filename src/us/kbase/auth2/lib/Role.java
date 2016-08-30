package us.kbase.auth2.lib;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
	ADMIN		("Admin"),
	DEV_TOKEN	("Create developer tokens"),
	SERV_TOKEN	("Create server tokens");
	
	private static final Map<String, Role> ROLE_MAP = new HashMap<>();
	static {
		for (final Role r: Role.values()) {
			ROLE_MAP.put(r.getRole(), r);
		}
	}
	
	private final String role;
	
	private Role(final String role) {
		this.role = role;
	}
	
	public String getRole() {
		return role;
	}
	
	public static Role getRole(final String role) {
		if (!ROLE_MAP.containsKey(role)) {
			throw new IllegalArgumentException("Invalid role: " + role);
		}
		return ROLE_MAP.get(role);
	}
	
	public static List<Role> grantedRoles(final Role role) {
		if (Role.ADMIN.equals(role)) {
			return Arrays.asList(Role.ADMIN, Role.SERV_TOKEN, Role.DEV_TOKEN);
		}
		if (Role.SERV_TOKEN.equals(role)) {
			return Arrays.asList(Role.SERV_TOKEN, Role.DEV_TOKEN);
		}
		if (Role.DEV_TOKEN.equals(role)) {
			return Arrays.asList((Role.DEV_TOKEN));
		}
		return new LinkedList<>();
	}
	
	public static boolean hasRole(
			final List<Role> possesed,
			final Role required) {
		final Set<Role> granted = possesed.stream()
				.flatMap(r -> grantedRoles(r).stream())
				.collect(Collectors.toSet());
		return granted.contains(required);
		
	}
}
