package com.tweetsearch.twitter;

import java.util.ArrayList;
import java.util.List;

public class TwitterSearchResult 
{
	private String nextResults;
	private String refreshURL;
	
	private List<Tweet> tweets = new ArrayList<Tweet>();
	
	public void setNextResults(String nextResults) {
		this.nextResults = nextResults;
	}
	
	public String getnextResults() {
		return nextResults;
	}
	
	public void setRefreshURL(String refreshURL) {
		this.refreshURL = refreshURL;
	}
	
	public String getRefreshURL() {
		return refreshURL;
	}
	
	public void setTweets(List<Tweet> tweets) {
		this.tweets = tweets;
	}
	
	public List<Tweet> getTweets() {
		return tweets;
	}
}
