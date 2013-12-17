package com.srain.sdk.image;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.srain.sdk.BuildConfig;
import com.srain.sdk.app.lifecycle.LifeCycleComponent;
import com.srain.sdk.image.ImageProvider.ImageCacheParams;

public class ImageLoader implements LifeCycleComponent {

	private static final String TAG = "cube_image";
	private ImageProvider mImageCache;
	private Worker mWorker;

	private boolean mExitTasksEarly = false;
	protected boolean mPauseWork = false;
	private final Object mPauseWorkLock = new Object();
	private final ImageLoadingGuard mImageLoadingGuard;

	public Resources mResources;

	public ImageLoader(Context context, Bitmap loadingBitmap, String cacheDir) {

		ImageCacheParams cacheParams = new ImageCacheParams(context, cacheDir);
		cacheParams.setMemCacheSizePercent(0.25f);
		mImageCache = new ImageProvider(cacheParams);

		mResources = context.getResources();
		mImageLoadingGuard = ImageLoadingGuard.create(mResources, loadingBitmap);

		mWorker = Worker.getInstance();
		new ImageCacheWork(ImageCacheWorkType.init_cache).run();
	}

	public void load(final ImageView imageView, String url, int requestWidth, int requestHeight, int debugIndex) {

		IloadHandler loadSuccHandler = new IloadHandler() {
			@Override
			public void onLoadSucc(ImageResult imageResult) {
				imageView.setImageDrawable(imageResult.getBitmapDrawable());
			}
		};

		ImageRequest imageRequest = new ImageRequest(url);
		imageRequest.setDebugIndex(debugIndex);
		imageRequest.setSize(requestWidth, requestHeight);

		BitmapDrawable value = null;
		if (mImageCache != null) {
			value = mImageCache.getBitmapFromMemCache(imageRequest);
		}

		ImageResult imageResult = new ImageResult(value);
		if (value != null) {
			loadSuccHandler.onLoadSucc(imageResult);
		} else if (cancelPotentialLastWork(imageView, imageRequest)) {
			ImageProcessor processor = new ImageProcessor(imageView, loadSuccHandler, imageRequest);
			mImageLoadingGuard.onLoading(imageView, processor);
			mWorker.doWork(processor);
		}
	}

	/**
	 * Returns true if the current work has been canceled or if there was no work in progress on this image view. Returns false if the work in progress deals with the same data. The work is not stopped in that case.
	 */
	private boolean cancelPotentialLastWork(ImageView imageView, ImageRequest imageRequest) {
		ImageProcessor work = ImageLoadingGuard.getImageViewBadage(imageView);

		// The same work is already in progress.
		if (work != null && work.getImageRequest() != null && work.getImageRequest().getCacheKey() == imageRequest.getCacheKey()) {
			Log.d(TAG, String.format("%s The same work is already in progress.", imageRequest));
			return false;
		} else {
			if (work != null) {
				Log.d(TAG, String.format("%s imageview is reused, the previous work is cancled.", imageRequest));
				mWorker.cancle(work);
			}
			return true;
		}
	}

	private class ImageProcessor extends WorkBase {

		private ImageRequest imageRequest;
		private ImageResult imageResult;

		private final WeakReference<ImageView> imageViewReference;
		private final WeakReference<IloadHandler> loadHandlerReference;

		public ImageProcessor(ImageView imageView, IloadHandler loadHandler, ImageRequest imageRequest) {
			this.imageRequest = imageRequest;
			imageViewReference = new WeakReference<ImageView>(imageView);
			loadHandlerReference = new WeakReference<IloadHandler>(loadHandler);
		}

		public ImageRequest getImageRequest() {
			return imageRequest;
		}

		@Override
		public void doInBackground() {
			Bitmap bitmap = null;

			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						Log.d(TAG, String.format("%s wait. isCancelled: %s mPauseWork: %s", imageRequest, isCancelled(), mPauseWork));
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			// If this task has not been cancelled by another
			// thread and the ImageView that was originally bound to this task is still bound back
			// to this task and our "exit early" flag is not set then try and fetch the bitmap from
			// the cache
			if (!isCancelled() && stillAttachingTheSameImageView() && !mExitTasksEarly) {
				bitmap = mImageCache.getBitmapFromDiskCache(imageRequest);
			}

			BitmapDrawable drawable = mImageCache.createBitmapDrawable(mResources, bitmap);
			mImageCache.addBitmapToMemCache(imageRequest.getCacheKey(), drawable);
			imageResult = new ImageResult(drawable);
		}

		/**
		 * Check if the ImageView associated with this task as long as the ImageView's task still points to this task as well.
		 */
		private boolean stillAttachingTheSameImageView() {
			final ImageView imageView = imageViewReference.get();
			final ImageProcessor work = ImageLoadingGuard.getImageViewBadage(imageView);
			if (this == work) {
				return true;
			}
			return false;
		}

		@Override
		public void onPostExecute() {
			IloadHandler loadHandler = loadHandlerReference.get();
			if (loadHandler != null && stillAttachingTheSameImageView()) {
				loadHandler.onLoadSucc(imageResult);
			}
		}
	}

	protected enum ImageCacheWorkType {
		init_cache, close_cache, flush_cache
	}

	private class ImageCacheWork extends WorkBase {

		ImageCacheWork(ImageCacheWorkType workType) {
			mWorkType = workType;
		}

		private ImageCacheWorkType mWorkType;

		@Override
		public void doInBackground() {

			switch (mWorkType) {
			case init_cache:
				mImageCache.initDiskCache();
				break;
			case close_cache:
				if (mImageCache != null) {
					mImageCache.close();
					mImageCache = null;
				}
				break;
			case flush_cache:
				if (mImageCache != null) {
					mImageCache.flush();
				}
				break;
			default:
				break;
			}
		}

		@Override
		public void onPostExecute() {
		}

		void run() {
			mWorker.doWork(this);
		}
	}

	/**
	 * temporarily hand up work
	 */
	public void pauseWork() {
		mPauseWork = true;
		mExitTasksEarly = false;
		new ImageCacheWork(ImageCacheWorkType.flush_cache).run();
	}

	public void resumeWork() {
		synchronized (mPauseWorkLock) {
			mPauseWork = false;
			mExitTasksEarly = false;
			mPauseWorkLock.notifyAll();
		}
	}

	public void clearCache() {
		mImageCache.clearCache();
	}

	@Override
	public void onResume() {
		mPauseWork = false;
		mExitTasksEarly = false;
	}

	@Override
	public void onStop() {
		mPauseWork = true;
		mExitTasksEarly = true;
		new ImageCacheWork(ImageCacheWorkType.flush_cache).run();
	}

	@Override
	public void onDestroy() {
		new ImageCacheWork(ImageCacheWorkType.close_cache).run();
	}
}
