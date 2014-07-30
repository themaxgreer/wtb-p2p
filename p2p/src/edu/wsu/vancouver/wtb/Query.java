package edu.wsu.vancouver.wtb;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.eclipse.jetty.util.URIUtil;

public class Query {
	private String q;
	private String uuid;
	private String destination;
	private String destinationPort;

	public Query(String q, String uuid, String destination) {
		this.q = q;
		this.uuid = uuid;
		this.destination = destination;
		if (destination == null) {
			this.destinationPort = String.valueOf(WoughTarfaBaris.port);
		}
	}

	public boolean match(SearchResult f) {
		//replace commmas with spaces
		String g = q.replace(',', ' ');
		//split the string at spaces, this allows users to enter x number of key words and
		// as long as one matches we are good
		String[] g2 = g.split(" ");
		for (String o : g2) {
			if (f.title.toLowerCase().contains(o.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public URI buildURI(URIBuilder base) throws URISyntaxException {
		URIBuilder tmp = base.addParameter("q", q).addParameter("uuid", uuid);
		if (destination != null) {
			tmp = tmp.addParameter("destination", destination);
		}
		else {
			tmp = tmp.addParameter("destinationPort", destinationPort);
		}
		return tmp.build();
	}

	public String getUUID() {
		return uuid;
	}

	public String getDestination() {
		return this.destination + "/results/" + URIUtil.encodePath(this.uuid);
	}
}
