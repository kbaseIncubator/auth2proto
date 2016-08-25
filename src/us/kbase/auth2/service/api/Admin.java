package us.kbase.auth2.service.api;

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
import us.kbase.auth2.lib.Password;
import us.kbase.auth2.lib.UserName;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.MissingParameterException;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;

@Path("/admin")
public class Admin {

	//TODO TEST
	//TODO JAVADOC

	//TODO NOW reset user pwd
	//TODO NOW revoke user token
	//TODO NOW find user
	//TODO NOW roles. Admin, create dev / server toke for now
	
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
	@Produces(MediaType.TEXT_HTML)
	@Template(name = "/adminlocalaccountcreated")
	public Map<String, String> createLocalAccountComplete(
			@FormParam("user") final String userName,
			@FormParam("full") final String fullName,
			@FormParam("email") final String email)
			throws AuthException, AuthStorageException {
		//TODO NOW check user is admin
		//TODO NOW log
		//TODO NOW email class with proper checking (probably not validation)
		if (userName == null) {
			throw new MissingParameterException("userName");
		}
		final Password pwd = auth.createLocalUser(
				new UserName(userName), fullName, email);
		final Map<String, String> ret = ImmutableMap.of(
				"user", userName,
				"full", fullName,
				"email", email,
				"password", new String(pwd.getPassword())); // char[] won't work
		pwd.clear(); // not that this helps much...
		return ret;
	}


}
