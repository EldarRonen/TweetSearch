package com.tweetsearch.image;

import android.graphics.Bitmap;
import android.util.LruCache;


public class BitmapCache {
	private static BitmapCache instance;

	private LruCache<String, Bitmap> memoryCache;

	private BitmapCache() {
		// Get max available VM memory
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 8;

		memoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	public static BitmapCache getInstamce() {
		if (instance == null) {
			synchronized (BitmapCache.class) {
				if (instance == null) {
					instance = new BitmapCache();
				}
			}
		}
		return instance;
	}
	
	public void addBitmapToMemoryCache(String url, Bitmap bitmap) {
	    if (getBitmapFromMemCache(url) == null) {
	    	memoryCache.put(url, bitmap);
	    }
	}
	
	public Bitmap getBitmapFromMemCache(String key) {
	    return memoryCache.get(key);
	}

}
