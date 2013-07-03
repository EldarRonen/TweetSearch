package com.tweetsearch.activity;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.tweetsearch.R;
import com.tweetsearch.image.ImageDownloader;
import com.tweetsearch.twitter.Tweet;
import com.tweetsearch.twitter.TwitterAPI;
import com.tweetsearch.twitter.TwitterSearchResult;

public class MainActivity extends ListActivity 
{
	public static final String EXTRA_TWEET = "com.tweetsearch.MainActivity.TWEET";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getActionBar().setIcon(R.drawable.twitter_logo);
				
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Because this activity has set launchMode="singleTop", the system calls this method
		// to deliver the intent if this activity is currently the foreground activity when
		// invoked again (when the user executes a search from this activity, we don't create
		// a new instance of this activity, so the system delivers the search intent here)
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(true);
		}

		return true;
	}
	
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	Intent intent = new Intent(this, TweetActivity.class);
    	Tweet tweet = (Tweet) getListAdapter().getItem(position);
    	intent.setAction(Intent.ACTION_VIEW);
    	intent.putExtra(EXTRA_TWEET, tweet);    	
    	startActivity(intent);
    }

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			setListAdapter(null); // clear previous search (this will remove previous result search views)
			 
			String query = intent.getStringExtra(SearchManager.QUERY);
			searchTwitter(query);
		}
	}

	private void searchTwitter(String query) {
		// check network connectivity
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {	
			new TwitterSearchTask(this).execute(query);
		} else {
			alertNetworkConnectivity();
		}	
	}

	private void alertNetworkConnectivity() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(R.string.alert_network_message)
			   .setTitle(R.string.alert_network_title)
		       .setIcon(R.drawable.alerts_and_states_error);

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private class TwitterSearchTask extends AsyncTask<String, Void, TwitterSearchResult> 
	{
		private final Activity activity;
		private final LinearLayout linlaHeaderProgress = (LinearLayout) findViewById(R.id.headerProgress);

		public TwitterSearchTask(Activity activity) {
			this.activity = activity;
		}
		
		@Override
		protected void onPreExecute() {    
		    // SHOW THE SPINNER WHILE LOADING FEEDS
		    linlaHeaderProgress.setVisibility(View.VISIBLE);
		}

		@Override
		protected TwitterSearchResult doInBackground(String... queries) {
			try {
				return TwitterAPI.search(queries[0]);			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(TwitterSearchResult result) {
			if (result == null) {
				// TODO: issue response to user
				return;
			}

			SearchResultAdapter adapter = new SearchResultAdapter(activity, R.layout.search_result, result.getTweets());
			setListAdapter(adapter);
			
			// HIDE THE SPINNER AFTER LOADING FEEDS
		    linlaHeaderProgress.setVisibility(View.GONE);
		}
	}

	private class SearchResultAdapter extends ArrayAdapter<Tweet> 
	{
		private final ImageDownloader imageDownloader;

		public SearchResultAdapter(Context context, int textViewResourceId, List<Tweet> tweets) {
			super(context, textViewResourceId, tweets);

			Bitmap placeHolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.twitter_bird_dark);
			imageDownloader = new ImageDownloader(context, placeHolder);
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.search_result, parent, false);
			}

			Tweet tweet = getItem(position);

			TextView userName = (TextView) view.findViewById(R.id.user_name);
			userName.setText(tweet.getUser().getName());
			
			TextView screenName = (TextView) view.findViewById(R.id.screen_name);
			screenName.setText(tweet.getUser().getScreenName());

			TextView text = (TextView) view.findViewById(R.id.text);
			text.setText(tweet.getText());

			ImageView image = (ImageView) view.findViewById(R.id.profile_image);
			imageDownloader.download(tweet.getUser().getProfileImageURL(), image);

			return view;
		}
	}
}
