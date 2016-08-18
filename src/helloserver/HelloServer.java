package helloserver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

import hellotest.RootServlet;

public class HelloServer {

	public static void main(String[] args) throws Exception {

		Server server = new Server(Integer.valueOf(args[0]));

		ServletContextHandler context = new ServletContextHandler();
		context.setContextPath("/");
		context.setResourceBase("./webapps/");
		server.setHandler(context);

		ServletHolder jerseyServlet = context.addServlet(
				ServletContainer.class, "/auth/*");
		jerseyServlet.setInitOrder(1);
		jerseyServlet.setInitParameter(
				"javax.ws.rs.Application", "hellotest.HelloApp");
		context.addServlet(
				DefaultServlet.class, "/assets/*");
		context.addServlet(RootServlet.class, "/*");
		server.start();
		server.join();
	}
}
