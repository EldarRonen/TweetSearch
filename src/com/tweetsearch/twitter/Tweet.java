package com.tweetsearch.twitter;

import android.os.Parcel;
import android.os.Parcelable;

public class Tweet implements Parcelable
{
	private User   user;
	private String text;
	private String createdAt;
	private String photoURL;
	
	private Tweet(TweetBuilder builder) {
		user = builder.user;
		text = builder.text;
		createdAt = builder.createdAt;
		photoURL = builder.photoURL;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getText() {
		return text;
	}

	public String getCreatedAt() {
		return createdAt;
	}
	
	public String getPhotoURL() {
		return photoURL;
	}
	
	public Tweet(Parcel in) {
		readFromParcel(in);
	}
	
	private void readFromParcel(Parcel in) {
		user = in.readParcelable(User.class.getClassLoader());
		text = in.readString();
		createdAt = in.readString();
		photoURL = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(user, flags);
		dest.writeString(text);
		dest.writeString(createdAt);
		dest.writeString(photoURL);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	public static final Parcelable.Creator<Tweet> CREATOR = new Creator<Tweet>() {
		
		@Override
		public Tweet[] newArray(int size) {
			return new Tweet[size];
		}
		
		@Override
		public Tweet createFromParcel(Parcel source) {
			return new Tweet(source);
		}
	};
	
	public static class TweetBuilder 
	{
		private User   user;
		private String text;
		private String createdAt;
		private String photoURL;
		
		public TweetBuilder user(User user) {
			this.user = user;
			return this;
		}
		
		public TweetBuilder text(String text) {
			this.text = text;
			return this;
		}
		
		public TweetBuilder createdAt(String createdAt) {
			this.createdAt = createdAt;
			return this;
		}
		
		public TweetBuilder photoURL(String url) {
			photoURL = url + ":medium";
			return this;
		}
		
		public Tweet build() {
			return new Tweet(this);
		}
	}
}
