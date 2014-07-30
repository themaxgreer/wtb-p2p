package edu.wsu.vancouver.wtb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Directory {
	private File dir;
	private static final List<SearchResult> emptyList = new ArrayList<SearchResult>();
	public Directory(String location) {
		dir = new File(location);
	}

	public List<SearchResult> search(Query q) {
		if (dir == null) {
			return emptyList;
		}

		File[] files = dir.listFiles();
		if (files == null) {
			return emptyList;
		}

		List<SearchResult> matchedFiles = new ArrayList<SearchResult>();
		for (File f : files) {
			SearchResult s = makeSearchResult(f);
			if (q.match(s)) {
				matchedFiles.add(s);
			}
		}
		return matchedFiles;
	}

	private static SearchResult makeSearchResult(File f) {
		String Hash = null;
		try {
			Hash = org.apache.commons.codec.digest.DigestUtils.sha512Hex(new FileInputStream(f));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new SearchResult(
				f.getName(),
				f.length(),
				"26 ms",
				(String) null,
				Hash
		);
	}
}
