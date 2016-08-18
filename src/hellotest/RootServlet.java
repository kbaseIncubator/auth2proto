package hellotest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class RootServlet extends HttpServlet {

	@Override
	protected void doGet(
			final HttpServletRequest request,
			final HttpServletResponse response)
			throws ServletException, IOException {
		response.getOutputStream().write("rootserv".getBytes());
	}
}
