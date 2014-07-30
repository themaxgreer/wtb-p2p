package edu.wsu.vancouver.wtb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

public class Download implements Runnable {
	private FileOutputStream file;
	private HttpClient hc;
	private HttpGet hg;
	private HttpResponse hr;
	private File outputFile;

	public enum StatusType {
		INACTIVE, ACTIVE, DONE, BROKEN,
	};

	public StatusType status;

	public Download(HttpClient hc, URI from, URI to) throws ClientProtocolException, IOException {
		this.hc = hc;
		this.hg = new HttpGet(from);
		this.status = StatusType.INACTIVE;
		this.outputFile = new File(to);
		this.file = new FileOutputStream(outputFile);
	}

	public void run() {
		try {
			status = StatusType.ACTIVE;
			hr = hc.execute(hg);
			hr.getEntity().writeTo(file);
			file.close();
			status = StatusType.DONE;
		}
		catch (ClientProtocolException e) {
			status = StatusType.BROKEN;
			e.printStackTrace();
		}
		catch (IOException e) {
			status = StatusType.BROKEN;
			e.printStackTrace();
		}
	}

	public long dataReceived() {
		return outputFile.length();
	}
}
