package edu.wsu.vancouver.wtb;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Peer {
	private static final List<SearchResult> emptyList = new ArrayList<SearchResult>();
	private String hostname;
	private int port;

	public Peer(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	public void search(final Query q) {
		new Thread(
			new Runnable() {
				public void run() {
					try {
						URIBuilder uri = new URIBuilder()
						.setScheme("http")
						.setHost(hostname)
						.setPort(port)
						.setPath("/search");
						URI searchURI = q.buildURI(uri);
						HttpClient client = new DefaultHttpClient();
						HttpGet request = new HttpGet(searchURI);
						client.execute(request);
					}
					catch (URISyntaxException e) {
						return;
					}
					catch (ClientProtocolException e) {
						return;
					}
					catch (IOException e) {
						return;
					}
				}
			}
		).start();
		return;
	}
}
