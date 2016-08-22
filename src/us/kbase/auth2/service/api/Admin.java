package us.kbase.auth2.service.api;

import java.util.HashMap;
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

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.service.exceptions.MissingParameterException;

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
	@Template(name = "/localaccount")
	@Produces(MediaType.TEXT_HTML)
	public Map<String, String> createLocalAccountStart(
			@QueryParam("admin") final String adminName) {
		//TODO NOW get adminName from token
		final Map<String, String> ret = new HashMap<>();
		ret.put("name", adminName);
		return ret;
		
	}
	
	@POST
	@Path("/localaccount/create")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Template(name = "/localaccountcreated")
	public Map<String, Object> createLocalAccountComplete(
			@FormParam("user") final String userName,
			@FormParam("full") final String fullName,
			@FormParam("email") final String email)
			throws MissingParameterException {
		
		final char[] pwd = auth.createLocalUser(userName, fullName, email);
		
		final Map<String, Object> ret = new HashMap<>();
		ret.put("user", userName);
		ret.put("full", fullName);
		ret.put("email", email);
		ret.put("password", new String(pwd)); // char[] won't work
		clear(pwd); // not that this helps much...
		return ret;
	}

	private void clear(char[] pwd) {
		for (int i = 0; i < pwd.length; i++) {
			pwd[i] = '0';
		}
	}
}
