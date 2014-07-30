package edu.wsu.vancouver.wtb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

///////////////
//Modification log
//	April 1, 2013 Brian Lamb
//		Added in 2 static variables mypeers and toEnd
//			mypeers will be the peer list and to End is used in Connection handler
//	April 2 2013,
//		add 2 new variables here(commented out)
//			one for setting up our shared list and another for the network
//		add another commented out variable for our Query List
///////////
public class WoughTarfaBaris {
	public static int port = 8080;
	public static void main(String[] args) throws Exception {
		List<Peer> peers = new ArrayList<Peer>();
		peers.add(new Peer("172.29.0.150", 8080));

		Directory dir = new Directory("/tmp/books/");

		String webDir = WoughTarfaBaris.class.getClassLoader().getResource("edu/wsu/vancouver/wtb/front").toString();
		Server server = new Server(WoughTarfaBaris.port);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.setResourceBase(webDir);
		server.setHandler(context);

		Map<String, List<SearchResult>> results =
				new HashMap<String, List<SearchResult>>();
		context.addServlet(new ServletHolder(new DefaultServlet()), "/wtb.js");
		context.addServlet(new ServletHolder(new DefaultServlet()), "/wtb.css");
		context.addServlet(new ServletHolder(new DefaultServlet()), "/wtb.html");
		context.addServlet(new ServletHolder(new DefaultServlet()), "/favicon.ico");
		context.addServlet(new ServletHolder(new SearchServlet(peers, dir, results)), "/search");
		context.addServlet(new ServletHolder(new TransferServlet("file:///tmp/books/")), "/transfer/*");
		context.addServlet(new ServletHolder(new FileHashServlet("/tmp/books/")), "/file/by-hash/*");
		context.addServlet(new ServletHolder(new ResultsServlet(results)), "/results/*");
		ServletHolder sh = new ServletHolder(new DefaultServlet());
		sh.setInitParameter("resourceBase", "/tmp/books/");
		sh.setInitParameter("pathInfoOnly", "true");
		context.addServlet(sh, "/file/by-title/*");

		server.start();
		server.join();
	}
}
