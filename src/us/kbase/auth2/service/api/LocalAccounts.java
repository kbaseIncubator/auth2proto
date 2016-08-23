package us.kbase.auth2.service.api;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.mvc.Template;
import org.glassfish.jersey.server.mvc.Viewable;

import com.google.common.collect.ImmutableMap;

import us.kbase.auth2.lib.AuthToken;
import us.kbase.auth2.lib.Authentication;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.service.exceptions.AuthenticationException;

@Path("/localaccount")
public class LocalAccounts {

	@Inject
	private Authentication auth;
	
	@GET
	@Path("/login")
	@Template(name = "/locallogin")
	@Produces(MediaType.TEXT_HTML)
	public Map<String, String> login() {
		return ImmutableMap.of("targeturl", "/localaccount/loginresult");
	}
	
	@POST
	@Path("/loginresult")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response loginResult(
			@FormParam("user") final String userName,
			@FormParam("pwd") String pwd, //char makes Jersey puke
			//checkbox, so "on" = checked, null = not checked
			@FormParam("stayLoggedIn") final String stayLoggedIn)
			throws AuthenticationException, AuthStorageException {
		final AuthToken t = auth.localLogin(userName, pwd.toCharArray());
		//TODO NOW log
		pwd = null; // try to get pwd GC'd as quickly as possible
		//TODO NOW if reset required, do reset
		return Response.ok(
				new Viewable("/localloginresult",
						ImmutableMap.of("user", t.getUserName())))
				.cookie(getCookie(t, stayLoggedIn == null))
				.build();
	}
	
	private NewCookie getCookie(final AuthToken t, final boolean session) {
		return new NewCookie(new Cookie("token", t.getToken()), "authtoken",
				getMaxAge(t, session), false); //TODO CONFIG make secure cookie configurable
	}
	
	private int getMaxAge(final AuthToken t, final boolean session) {
		if (session) {
			return NewCookie.DEFAULT_MAX_AGE;
		}
		final long exp = t.getExpirationDate().getTime() -
				new Date().getTime();
		if (exp > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) exp;
	}
	
}
