package edu.wsu.vancouver.wtb;

import java.util.List;

public class ResultSet {
	public int port;
	public List<SearchResult> results;

	public ResultSet(int port, List<SearchResult> results) {
		this.port = port;
		this.results = results;
	}
}
