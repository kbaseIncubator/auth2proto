package us.kbase.auth2.service.api;

import static us.kbase.auth2.lib.Utils.clear;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.mvc.Template;

import com.google.common.collect.ImmutableMap;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.service.exceptions.AuthException;

@Path("/admin")
public class Admin {

	@Inject
	private Authentication auth;
	
	@GET
	public String admin() {
		return "foo";
	}
	
	@GET
	@Path("/localaccount")
	@Template(name = "/adminlocalaccount")
	@Produces(MediaType.TEXT_HTML)
	public Map<String, String> createLocalAccountStart(
			@QueryParam("admin") final String adminName) {
		//TODO NOW get adminName from token
		return ImmutableMap.of("name", adminName,
				"targeturl", "/admin/localaccount/create");
	}
	
	@POST
	@Path("/localaccount/create")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Template(name = "/adminlocalaccountcreated")
	public Map<String, String> createLocalAccountComplete(
			@FormParam("user") final String userName,
			@FormParam("full") final String fullName,
			@FormParam("email") final String email)
			throws AuthException, AuthStorageException {
		//TODO NOW check user is admin
		//TODO NOW log
		
		final char[] pwd = auth.createLocalUser(userName, fullName, email);
		final Map<String, String> ret = ImmutableMap.of(
				"user", userName,
				"full", fullName,
				"email", email,
				"password", new String(pwd)); // char[] won't work
		clear(pwd); // not that this helps much...
		return ret;
	}


}
