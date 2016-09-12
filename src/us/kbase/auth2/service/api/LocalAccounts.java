package us.kbase.auth2.service.api;

import static us.kbase.auth2.service.api.APIUtils.getLoginCookie;

import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import com.google.common.collect.ImmutableMap;

import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.Password;
import us.kbase.auth2.lib.UserName;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.exceptions.MissingParameterException;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.token.NewToken;

@Path("/localaccount")
public class LocalAccounts {
	
	//TODO TEST
	//TODO JAVADOC

	//TODO NOW reset pwd

	@Inject
	private Authentication auth;
	
	@GET
	@Path("/login")
	@Template(name = "/locallogin")
	@Produces(MediaType.TEXT_HTML)
	public Map<String, String> login() {
		return ImmutableMap.of("targeturl", "/localaccount/login/result");
	}
	
	@POST
	@Path("/login/result")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response loginResult(
			@FormParam("user") final String userName,
			@FormParam("pwd") String pwd, //char makes Jersey puke
			//checkbox, so "on" = checked, null = not checked
			@FormParam("stayLoggedIn") final String stayLoggedIn)
			throws AuthStorageException, MissingParameterException,
			AuthenticationException {
		if (userName == null || userName.isEmpty()) {
			throw new MissingParameterException("user");
		}
		if (pwd == null || pwd.isEmpty()) {
			throw new MissingParameterException("pwd");
		}
		final NewToken t = auth.localLogin(new UserName(userName),
				new Password(pwd.toCharArray()));
		//TODO NOW log
		pwd = null; // try to get pwd GC'd as quickly as possible
		//TODO NOW if reset required, do reset
		return Response.ok(
				new Viewable("/localloginresult",
						ImmutableMap.of("user", userName)))
				.cookie(getLoginCookie(t, stayLoggedIn == null))
				.build();
	}
}
