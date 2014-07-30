package edu.wsu.vancouver.wtb;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

public class PeerServices {
	private List<Peer> peers;

	public PeerServices(List<Peer> peers) {
		this.peers = peers;
		// Initialize peers here. Load host-names from flat-file.
		FileInputStream f = null;
		BufferedReader b;
		try {
			String line;
			f = new FileInputStream("initHosts.txt");
			b = new BufferedReader(new InputStreamReader(f,
					Charset.forName("UTF-8")));
			// Add to peer list.
			while ((line = b.readLine()) != null) {
				// System.out.println(line);
				Peer p = new Peer(line, 80);
				peers.add(p);
			}
			b.close();
			f.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void removePeers() throws URISyntaxException {
		// Removes peers that for connections cannot be made.
		Iterator<Peer> i = peers.iterator();
		while (i.hasNext()) {
			Peer peer = i.next();
			try {
				URIBuilder builder = new URIBuilder();
				builder.setScheme("http").setHost(peer.getHostname())
						.setPort(8080).setPath("/peeracq");
				URI uri = builder.build();
				URL url = new URL(uri.toASCIIString());
				HttpURLConnection urlConn = (HttpURLConnection) url
						.openConnection();
				// Magic number timeout in milliseconds.
				urlConn.setConnectTimeout(300);
				urlConn.connect();
				urlConn.disconnect();
			} catch (SocketTimeoutException e) {
				// e.printStackTrace();
				// System.out.println("Cannot connect to peer "
				// + peer.getHostname() + " .");
				i.remove();
			} catch (IOException e) {
				// e.printStackTrace();
			} catch (URISyntaxException e) {
				// e.printStackTrace();
			}
		}
	}

	public void getPeers(String hostname) throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http").setHost(hostname).setPort(8080)
				.setPath("/peeracq");
		URI uri = builder.build();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(uri);
		// System.out.println(httpget.getURI());
		HttpResponse response;
		StringWriter writer = new StringWriter();
		try {
			response = client.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream istream = entity.getContent();
				IOUtils.copy(istream, writer, "UTF-8");
				String s = writer.toString();
				String[] strSplit = s.split("\n");
				for (String host : strSplit) {
					host = host.trim();
					// n by m search, string comparison
					boolean inList = false;
					for (Peer np : peers) {
						inList = false;
						if (np.getHostname().compareTo(host) == 0) {
							inList = true;
							break;
						}
					}
					if (inList == false) {
						Peer np = new Peer(host, 80);
						peers.add(np);
					}
				}
			}
			// printPeers();
		} catch (IOException e) {
			System.out.println(e.getCause());
			e.printStackTrace();
		}
	}

	void printPeers() {
		for (Peer p : peers) {
			System.out.println(p.getHostname());
		}
	}
}
