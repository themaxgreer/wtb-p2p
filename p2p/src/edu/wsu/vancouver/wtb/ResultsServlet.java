package edu.wsu.vancouver.wtb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ResultsServlet extends HttpServlet {
	private Map<String, List<SearchResult>> results;

	public ResultsServlet(Map<String, List<SearchResult>> results) {
		this.results = results;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] path = request.getPathInfo().split("/");
		if (path.length != 2) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String uuid = path[1];

		// TODO: thread safety
		List<SearchResult> previousResults = results.get(uuid);
		if (previousResults == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		Gson gs = new Gson();
		Scanner s = new Scanner(request.getInputStream());
		s.useDelimiter("\\A");
		if (!s.hasNext()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			s.close();
			return;
		}

		String contents = s.next();
		s.close();
		ResultSet rs = gs.fromJson(contents, new TypeToken<ResultSet>() {}.getType());
		for (SearchResult f : rs.results) {
			boolean newFile = true;
			if (f.sources.contains(null)) {
				f.sources.remove(null);
				f.sources.add("http://" + request.getRemoteAddr() + ":" + rs.port);
			}
			for (SearchResult g : previousResults) {
				if (f.filesize == g.filesize && f.SHA512Hash.equals(g.SHA512Hash)) {
					newFile = false;
					g.sources.addAll(f.sources);
					break;
				}
			}
			if (newFile) {
				previousResults.add(f);
			}
		}

		response.setStatus(HttpServletResponse.SC_OK);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] path = request.getPathInfo().split("/");
		if (path.length != 2) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String uuid = path[1];
		List<SearchResult> files = results.get(uuid);
		if (files == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setContentType("text/plain");
		response.setStatus(HttpServletResponse.SC_OK);
		Gson gs = new Gson();
		response.getWriter().print(gs.toJson(files));
	}
}
