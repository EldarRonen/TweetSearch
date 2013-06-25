TweetSearch 
==================

Android app to search twitter for recent tweets.
min SDK 14
target SDK 17
tested and run on android 4.1.2

this app uses sigle top activity - MainActivity.
user enter input through the ActionBar search functionality

main classes:
	MainActivity - recieve queries from the ActionBar search and display result.
		       extends ListActivity 
	
	TwitterSearchTask - Asynctask to send search requests to Twitter
	TwitterAPI - helper class to handle the HTTP requests to Twitter and parsing the responses
	ImageDownloader - helper class to download images from URL asynchronously



Known Limitation:
1. profile images are not cached (reloaded each time the view is displayed)
2. views in ListView are not recycled 
(I had some problems with displaying default image while image is downloaded, so I omitted the caching and recycle code eventually)

3. no feedback to the user when search returned no results or search query is illegal (only alert dialog when no internet connection)
4. infinite scroll not implemented.


