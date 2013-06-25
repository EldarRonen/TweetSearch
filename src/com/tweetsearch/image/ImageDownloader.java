package com.tweetsearch.image;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest file.</p>
 */
public class ImageDownloader 
{
	private static final String LOG_TAG = "ImageDownloader";

	private final Context context;
	private final Bitmap  placeHolderBitmap;

	private final BitmapCache cache = BitmapCache.getInstamce();

	public ImageDownloader(Context context, Bitmap placeHolder) {
		this.context = context; 
		this.placeHolderBitmap = placeHolder;		
	}

	/**
	 * Download the specified image from the Internet and binds it to the provided ImageView. The
	 * binding is immediate if the image is found in the cache and will be done asynchronously otherwise. 
	 *
	 * @param url The URL of the image to download.
	 * @param imageView The ImageView to bind the downloaded image to.
	 */
	public void download(String url, ImageView imageView) {
		Bitmap bitmap = cache.getBitmapFromMemCache(url);

		if (bitmap == null) {
			forceDownload(url, imageView);
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
	}


	/**
	 * Same as download but the image is always downloaded and the cache is not used.
	 */
	private void forceDownload(String url, ImageView imageView) {
		// State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
		if (url == null) {
			imageView.setImageDrawable(null);
			return;
		}

		if (cancelPotentialDownload(url, imageView)) {
			final BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			final DownloadedDrawable downloadedDrawable = new DownloadedDrawable(context.getResources(), placeHolderBitmap, task);
			imageView.setImageDrawable(downloadedDrawable);

			task.execute(url);
		}
	}

	/**
	 * Returns true if the current download has been canceled or if there was no download in
	 * progress on this image view.
	 * Returns false if the download in progress deals with the same url. The download is not
	 * stopped in that case.
	 */
	private static boolean cancelPotentialDownload(String url, ImageView imageView) {
		final BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				// Cancel previous task
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		// No task associated with the ImageView, or an existing task was cancelled
		return true;
	}

	/**
	 * @param imageView Any imageView
	 * @return Retrieve the currently active download task (if any) associated with this imageView.
	 * null if there is no such task.
	 */
	private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				final DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private String url;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			url = params[0];
			return downloadBitmap(url);
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
				return;  // TODO - should return ?
			}

			//cache.addBitmapToMemoryCache(url, bitmap);  // TODO - check why this cause null exception. moved inside the 'if' for now

			if (imageViewReference != null && bitmap != null) {
				cache.addBitmapToMemoryCache(url, bitmap);
				
				ImageView imageView = imageViewReference.get();
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

				// Change bitmap only if this process is still associated with it
				// Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
				if (this == bitmapDownloaderTask && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	/**
	 * A default Drawable that will be attached to the imageView while the download is in progress.
	 *
	 * <p>Contains a reference to the actual download task, so that a download task can be stopped
	 * if a new binding is required, and makes sure that only the last started download process can
	 * bind its result, independently of the download finish order.</p>
	 */
	private static class DownloadedDrawable extends BitmapDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(Resources resources, Bitmap placeHolder,BitmapDownloaderTask bitmapDownloaderTask) {
			super(resources, placeHolder);
			//setBounds(0, 0, placeHolder.getWidth(), placeHolder.getHeight());

			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}

	private Bitmap downloadBitmap(String urlString) {
		Bitmap bitmap = null;

		try {
			URL url = new URL(urlString);

			InputStream in = null;
			try {
				in = url.openStream();
				bitmap =  BitmapFactory.decodeStream(in);
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (MalformedURLException e) {
			Log.w(LOG_TAG, "Incorrect URL: " + urlString);
		} catch (IOException e) {
			Log.e(LOG_TAG, "I/O error while retrieving bitmap from: " + urlString);
			e.printStackTrace();
		} 

		return bitmap;
	}



	//    public static void download(String url, ImageView imageView) {
	//        BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
	//        task.execute(url);
	//    }
	//    
	//    private static class BitmapDownloaderTask extends AsyncTask <String, Void, Bitmap> 
	//    {
	//    	private static final String LOG_TAG = "BitmapDownloaderTask";
	//    	
	//    	private final ImageView imageView;
	//
	//        public BitmapDownloaderTask(ImageView imageView) {
	//            this.imageView = imageView;
	//        }
	//
	//        @Override
	//        protected Bitmap doInBackground(String... params) {
	//        	Bitmap bitmap = null;
	//            
	//            try {
	//                InputStream in = new URL(params[0]).openStream();
	//                bitmap = BitmapFactory.decodeStream(in);
	//            } catch (Exception e) {
	//                Log.e(LOG_TAG, e.getMessage());
	//                e.printStackTrace();
	//            }
	//            return bitmap;
	//        }
	//
	//        @Override
	//        protected void onPostExecute(Bitmap bitmap) {
	//            if ( bitmap != null) {
	//                imageView.setImageBitmap(bitmap);
	//            }
	//        }
	//
	//    }

}
