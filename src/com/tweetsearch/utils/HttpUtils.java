package com.tweetsearch.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpUtils 
{
	private static final String LOG_TAG = "HttpUtils";

	public static String getRequest(URL url) {
		HttpURLConnection conn = null;

		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);

			// Starts the query
			conn.connect();
			int response = conn.getResponseCode();
			Log.d(LOG_TAG, "http response code: " + response);

			if (response != 200) {
				Log.e(LOG_TAG, "http response Error: " + conn.getResponseMessage());
				return null;
			}

			return readContent(conn);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private static String readContent(HttpURLConnection conn) throws IOException {
		InputStream in = new BufferedInputStream(conn.getInputStream());

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder content = new StringBuilder();

			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line);
			}

			return content.toString();
			
		} finally {
			if (in != null)
				in.close();
		}
	}
}
