package edu.wsu.vancouver.wtb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.URIUtil;

public class FileHashServlet extends HttpServlet {
	private File dir;

	public FileHashServlet(String path) {
		this.dir = new File(path);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String[] path = request.getPathInfo().split("/");
		if (path.length != 2) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String hash = path[1];
		if (hash == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		File[] files = dir.listFiles();
		if (files == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}

		for (File f : files) {
			String fileHash;
			try {
				fileHash = org.apache.commons.codec.digest.DigestUtils.sha512Hex(new FileInputStream(f));
			} catch (IOException e) {
				continue;
			}

			if (fileHash.equals(hash)) {
				response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
				response.setHeader("Location", "/file/by-title/" + URIUtil.encodePath(f.getName()));
				return;
			}
		}

		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return;
	}
}
