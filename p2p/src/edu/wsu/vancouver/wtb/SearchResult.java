package edu.wsu.vancouver.wtb;

import java.util.HashSet;
import java.util.Set;

//added new vairable,source will be needed
public class SearchResult {
	public String title;
	public long filesize;
	public String eta;
	public Set<String> sources;
	public String SHA512Hash;

	public SearchResult(String title, long filesize, String eta, String source, String Hash) {
		Set<String> sources = new HashSet<String>();
		sources.add(source);

		this.title = title;
		this.filesize = filesize;
		this.eta = eta;
		this.sources = sources;
		this.SHA512Hash = Hash;
	}

	public SearchResult(String title, long filesize, String eta, Set<String> sources, String Hash) {
		this.title = title;
		this.filesize = filesize;
		this.eta = eta;
		this.sources = sources;
		this.SHA512Hash = Hash;
	}
}
