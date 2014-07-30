package edu.wsu.vancouver.wtb;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.util.URIUtil;

import com.google.gson.Gson;

public class TransferServlet extends HttpServlet {
	private String destinationDirectory;
	private DefaultHttpClient httpClient;
	private Map<String, Download> currentDownloads = new HashMap<>();

	TransferServlet(String destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
		this.httpClient = new DefaultHttpClient();
	}

	private void showTransferInfo(HttpServletResponse response, String transferId) throws IOException {
		Download d = currentDownloads.get(transferId);
		if (d == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(d.dataReceived());
	}

	private String spawnTransfer(String hash, String source, String title) {
		String transferId = UUID.randomUUID().toString();
		Download d;

		try {
			d = new Download(
					httpClient,
					new URIBuilder(source).setPath("/file/by-hash/" + hash).build(),
					URI.create(destinationDirectory + "/" + URIUtil.encodePath(title))
			);
		}
		catch (IOException e) {
			return null;
		}
		catch (URISyntaxException e) {
			return null;
		}

		currentDownloads.put(transferId, d);
		new Thread(d).start();
		return transferId;
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] path = request.getPathInfo().split("/");
		if (path.length == 0) {
			String hash = request.getParameter("hash");
			String source = request.getParameter("source");
			String title = request.getParameter("title");
			if (hash == null || source == null || title == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}

			String transferId = spawnTransfer(hash, source, title);
			if (transferId == null) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}

			response.setStatus(HttpServletResponse.SC_OK);
			Gson gs = new Gson();
			response.getWriter().print(gs.toJson(transferId));
		}
		else if (path.length == 2) {
			showTransferInfo(response, path[1]);
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] path = request.getPathInfo().split("/");

		if (path.length == 1) {
			showTransferInfo(response, path[1]);
		}
		else if (path.length == 2) {
			showTransferInfo(response, path[1]);
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
