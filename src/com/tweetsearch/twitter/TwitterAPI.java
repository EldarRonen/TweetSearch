package com.tweetsearch.twitter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import com.tweetsearch.twitter.Tweet.TweetBuilder;
import com.tweetsearch.twitter.User.UserBuilder;

import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;

public class TwitterAPI 
{
	private static final String CONSUMER_KEY = "3kerFSImjjeuEcXtFmCUA";
	private static final String CONSUMER_SECRET = "eYLiPZstIXHPlrxbVj5Pxpw4ABw0eOLCS6JAu0t8";
	private static final String ACCESS_TOKEN = "AAAAAAAAAAAAAAAAAAAAAGe5RQAAAAAAxmoxXIIrJ4bryNgxDc%2FqSXkQoOU%3DpQbQPbPqWq3CgUapUKYAh3CPqFGnuXfu7sqX6jcs6s";

	//private static final String API_REQUEST_TOKEN_URL = "https://api.twitter.com/oauth/request_token";
	//private static final String API_AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";
	private static final String API_OAUTH2_TOKEN_URL = "https://api.twitter.com/oauth2/token";

	private static final String TWITTER_API_URL = "https://api.twitter.com/1.1/search/tweets.json";
	private static final String LOG_TAG = "TwitterAPI";


	/*
	 *  curl --get 'https://api.twitter.com/1.1/search/tweets.json' 
	 *       --data 'q=NYC' 
	 *       --header 'Authorization: Bearer AAAAAAAAAAAAAAAAAAAAAGe5RQAAAAAAxmoxXIIrJ4bryNgxDc%2FqSXkQoOU%3DpQbQPbPqWq3CgUapUKYAh3CPqFGnuXfu7sqX6jcs6s' 
	 *       --verbose
	 */	
	public static TwitterSearchResult search(String query) throws IOException {		
		InputStream in = null;

		try {
			query = URLEncoder.encode(query, "utf-8");
			URL url = new URL(TWITTER_API_URL + "?q=" + query);

			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
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

			in = new BufferedInputStream(conn.getInputStream());
			return readJsonStream(in);
		} 
		catch (IOException e) {
			Log.e(LOG_TAG, "request failed for query: " + query + ". Error: " + e.getMessage());
			e.printStackTrace();
		} 
		finally {
			if (in != null) {
				in.close();
			}
		}

		return null;
	}

	private static TwitterSearchResult readJsonStream(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
		try {
			return readPage(reader);
		}
		finally {
			reader.close();
		}
	}

	private static TwitterSearchResult readPage(JsonReader reader) throws IOException {
		TwitterSearchResult result = new TwitterSearchResult();

		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();

			if (name.equals("search_metadata")) {
				readSearchMetaData(reader, result);
			}
			else if (name.equals("statuses")) {
				readStatuses(reader, result);
			} 
			else {
				reader.skipValue();
			}			
		}

		reader.endObject();
		return result;
	}

	private static void readSearchMetaData(JsonReader reader, TwitterSearchResult result) throws IOException {
		reader.beginObject();			

		while(reader.hasNext()) {
			String name = reader.nextName();

			if (name.equals("next_results")) {
				result.setNextResults(reader.nextString());
			} 
			else if (name.equals("refresh_url")) {
				result.setRefreshURL(reader.nextString());
			}
			else {
				reader.skipValue();
			}
		}

		reader.endObject();
	}

	private static void readStatuses(JsonReader reader, TwitterSearchResult result) throws IOException {
		List<Tweet> tweets = new ArrayList<Tweet>();

		reader.beginArray();
		while(reader.hasNext()) {
			tweets.add(readTweet(reader));
		}
		reader.endArray();

		result.setTweets(tweets);
	}

	private static Tweet readTweet(JsonReader reader) throws IOException {
		TweetBuilder builder = new TweetBuilder();

		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("created_at")) {
				builder.createdAt(reader.nextString());
			}
			else if (name.equals("text")) {
				builder.text(reader.nextString());
			}
			else if (name.equals("user")) {
				builder.user(readUser(reader));
			}
			else if (name.equals("entities")) {
				readEntities(reader, builder);
			}
			else {
				reader.skipValue();
			}
		}

		reader.endObject();
		return builder.build();
	}

	private static void readEntities(JsonReader reader, TweetBuilder builder) throws IOException {
		reader.beginObject();			

		while(reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("media")) {
				readMediaEnteties(reader, builder);
			}
			else {
				reader.skipValue();
			}
		}
		
		reader.endObject();
	}
	
	private static void readMediaEnteties(JsonReader reader, TweetBuilder builder) throws IOException {
		reader.beginArray();
		
		while(reader.hasNext()) {
			readMediaEntetie(reader, builder);
		}
		reader.endArray();
	}

	private static void readMediaEntetie(JsonReader reader, TweetBuilder builder) throws IOException {
		String mediaURL = null;
		String mediaType = null;
		
		reader.beginObject();			

		while(reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("media_url")) {
				mediaURL = reader.nextString();
			}
			else if (name.equals("type")) {
				mediaType = reader.nextString();
			}
			else {
				reader.skipValue();
			}
		}
		
		if ("photo".equals(mediaType)) {
			builder.photoURL(mediaURL);
		}
		else if (mediaType != null) {
			Log.w(LOG_TAG, "unsupported media type: " + mediaType + ". url: " + mediaURL);
		}
		
		reader.endObject();
	}

	private static User readUser(JsonReader reader) throws IOException {
		UserBuilder builder = new UserBuilder();
		
		reader.beginObject();
		while(reader.hasNext()) {
			String name = reader.nextName();

			if (name.equals("name")) {
				builder.name(reader.nextString());
			}
			else if (name.equals("screen_name")) {
				builder.screenName(reader.nextString());
			}
			else if (name.equals("profile_image_url")) {
				builder.profileImageURL(reader.nextString());
			}
			else if (name.equals("profile_banner_url")) {
				builder.profileBannerURL(reader.nextString());
			}
			else if (name.equals("description")) {
				builder.description(reader.nextString());
			}
			else if (name.equals("followers_count")) {
				builder.followersCount(reader.nextString());
			}
			else if (name.equals("friends_count")) {
				builder.friendsCount(reader.nextString());
			}
			else if (name.equals("statuses_count")) {
				builder.statusesCount(reader.nextString());
			}
			else {
				reader.skipValue();
			}			
		}
		reader.endObject();
		
		return builder.build();
	}

	/*
	 * curl --request 'POST' 'https://api.twitter.com/oauth2/token' 
	 *      --data 'grant_type=client_credentials' 
	 *      --header 'Authorization: Basic M2tlckZTSW1qamV1RWNYdEZtQ1VBOmVZTGlQWnN0SVhIUGxyeGJWajVQeHB3NEFCdzBlT0xDUzZKQXUwdDg=, 
	 *                Content-Type: application/x-www-form-urlencoded;charset=UTF-8' 
	 *      --verbose
	 */
	public static void getBearerToken() {		
		try {
			String credentials = String.format("%s:%s", URLEncoder.encode(CONSUMER_KEY, "UTF-8"), URLEncoder.encode(CONSUMER_SECRET, "UTF-8"));
			byte[] data = credentials.getBytes("UTF-8");
			credentials = Base64.encodeToString(data, Base64.DEFAULT);

			String body = "grant_type=client_credentials";

			URL url = new URL(API_OAUTH2_TOKEN_URL);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			//conn.setReadTimeout(10000 /* milliseconds */);
			//conn.setConnectTimeout(15000 /* milliseconds */);			
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
			conn.setRequestProperty("Authorization", "Basic " + credentials);			
			conn.setFixedLengthStreamingMode(body.getBytes().length);

			//Send request
			OutputStream out = new BufferedOutputStream(conn.getOutputStream());
			out.write(body.getBytes("UTF-8"));
			out.close ();

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {			
				//Get Response	
				InputStream is = new BufferedInputStream(conn.getInputStream());

				//JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
			}
			else {
				Log.e(LOG_TAG, "Http response code: " + conn.getResponseCode() + ", " + conn.getResponseMessage());
			}


		} catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, "Failed to encode credentials");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Malformed URL: " + e.getMessage());
			e.printStackTrace();
		} catch (ProtocolException e) {
			Log.e(LOG_TAG, "Protocol error: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error opening connection: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
