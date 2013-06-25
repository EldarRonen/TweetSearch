package com.tweetsearch.twitter;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable
{
	private String name;
	private String screenName;
	private String profileImageURL;
	private String profileBannerURL;
	private String description;
	private String followersCount;
	private String friendsCount;
	private String statusesCount;

	private User(UserBuilder builder) {
		name = builder.name;
		screenName = builder.screenName;
		profileImageURL = builder.profileImageURL;
		profileBannerURL = builder.profileBannerURL;
		description = builder.description;
		followersCount = builder.followersCount;
		friendsCount = builder.friendsCount;
		statusesCount = builder.statusesCount;
	}

	public String getName() {
		return name;
	}

	public String getScreenName() {
		return screenName;
	}

	public String getProfileImageURL() {
		return profileImageURL;
	}

	public String getProfileBannerURL() {
		return profileBannerURL;
	}

	public String getDescription() {
		return description;
	}

	public String getFollowersCount() {
		return followersCount;
	}

	public String getFriendsCount() {
		return friendsCount;
	}

	public String getStatusesCount() {
		return statusesCount;
	}

	public User(Parcel in) {  
		readFromParcel(in);  
	} 

	private void readFromParcel(Parcel in) {
		name = in.readString();
		screenName = in.readString();
		profileImageURL = in.readString();
		profileBannerURL = in.readString();
		description = in.readString();
		followersCount = in.readString();
		friendsCount = in.readString();
		statusesCount = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(screenName);
		dest.writeString(profileImageURL);
		dest.writeString(profileBannerURL);
		dest.writeString(description);
		dest.writeString(followersCount);
		dest.writeString(friendsCount);
		dest.writeString(statusesCount);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Parcelable.Creator<User> CREATOR = new Creator<User>() {
		
		@Override
		public User[] newArray(int size) {
			return new User[size];
		}
		
		@Override
		public User createFromParcel(Parcel source) {
			return new User(source);
		}
	};
	
	public static class UserBuilder 
	{
		private String name;
		private String screenName;		
		private String profileImageURL;
		private String profileBannerURL;
		private String description;
		private String followersCount;
		private String friendsCount;
		private String statusesCount;

		public UserBuilder name(String name) {
			this.name = name;
			return this;
		}

		public UserBuilder screenName(String screenName) {
			this.screenName = "@" + screenName;
			return this;
		}

		public UserBuilder profileImageURL(String url) {
			profileImageURL = url.replace("_normal.", "_bigger.");
			return this;
		}

		public UserBuilder profileBannerURL(String url) {
			profileBannerURL = url + "/mobile";
			return this;
		}

		public UserBuilder description(String description) {
			this.description = description;
			return this;
		}

		public UserBuilder followersCount(String count) {
			followersCount = count;
			return this;
		}

		public UserBuilder friendsCount(String count) {
			friendsCount = count;
			return this;
		}

		public UserBuilder statusesCount(String count) {
			statusesCount = count;
			return this;
		}

		public User build() {
			return new User(this);
		}
	}
}
