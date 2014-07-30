package edu.wsu.vancouver.wtb;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

public class SearchServlet extends HttpServlet {
	private List<Peer> peers;
	private Directory dir;
	private Set<String> previouslySeenQueries;
	private Map<String, List<SearchResult>> results;

	public SearchServlet(List<Peer> peers, Directory dir, Map<String, List<SearchResult>> results) {
		this.peers = peers;
		this.dir = dir;
		this.previouslySeenQueries = new HashSet<String>();
		this.results = results;
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println(request.toString());

		if (request.getParameter("q") == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String uuid = request.getParameter("uuid");
		String destination = request.getParameter("destination");

		if (uuid == null) {
			uuid = UUID.randomUUID().toString();
			results.put(uuid, new ArrayList<SearchResult>());
			previouslySeenQueries.add(uuid);
		}
		else {
			if (previouslySeenQueries.contains(uuid)) {
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			}
			previouslySeenQueries.add(uuid);
			if (destination == null) {
				destination = "http://" + request.getRemoteHost() + ":" + request.getParameter("destinationPort");
			}
		}

		Query q = new Query(request.getParameter("q"), uuid, destination);
		for (Peer p : peers) {
			p.search(q);
		}

		Gson gs = new Gson();
		if (destination != null) {
			ResultSet results = new ResultSet(
					WoughTarfaBaris.port,
					dir.search(q)
			);
			HttpClient client = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(q.getDestination());
			StringEntity postData = new StringEntity(gs.toJson(results));
			postData.setContentType("application/json");
			postRequest.setEntity(postData);
			client.execute(postRequest);
		}

		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(gs.toJson(q.getUUID()));
	}
}
