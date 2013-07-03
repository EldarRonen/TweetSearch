package com.tweetsearch.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tweetsearch.R;
import com.tweetsearch.image.ImageDownloader;
import com.tweetsearch.twitter.Tweet;

public class TweetActivity extends Activity 
{
	private static final String LOG_TAG = "TweetActivity";
	
	private ImageDownloader imageDownloader;
	private ImageDownloader tweetPhotoDownloader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet);
		overridePendingTransition(R.anim.slide_in_right_to_left,R.anim.out_stand_still);
		
		// Show the Up button in the action bar.
		setupActionBar();
		
		// initialize profile image downLoader
		Bitmap placeHolder = BitmapFactory.decodeResource(getResources(), R.drawable.twitter_bird_dark);
		imageDownloader = new ImageDownloader(this, placeHolder);
		
		Bitmap photoPlaceHolder = BitmapFactory.decodeResource(getResources(), R.drawable.content_picture);
		tweetPhotoDownloader = new ImageDownloader(this,  photoPlaceHolder);
		
		handleIntent(getIntent());
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			Tweet tweet = (Tweet) intent.getExtras().get(MainActivity.EXTRA_TWEET);
			displayTweet(tweet);
		}
		else {
			Log.w(LOG_TAG, "unsuportted intent action: " + intent.getAction());
		}
	}

	private void displayTweet(Tweet tweet) {
		TextView userName = (TextView) findViewById(R.id.user_name);
		userName.setText(tweet.getUser().getName());
		
		TextView screenName = (TextView) findViewById(R.id.screen_name);
		screenName.setText(tweet.getUser().getScreenName());

		TextView text = (TextView) findViewById(R.id.text);
		text.setText(tweet.getText());
		
		TextView createdAt = (TextView) findViewById(R.id.created_at);
		createdAt.setText(tweet.getCreatedAt()); // TODO format time

		ImageView image = (ImageView) findViewById(R.id.profile_image);
		imageDownloader.download(tweet.getUser().getProfileImageURL(), image);
		
		if (tweet.getPhotoURL() != null) {
			ImageView photoView = (ImageView) findViewById(R.id.media_image);
			photoView.setVisibility(View.VISIBLE);
			tweetPhotoDownloader.download(tweet.getPhotoURL(), photoView);
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
