package com.srain.cube.image;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {

	/**
	 * A handler that used in loading a image.
	 * 
	 * <p>
	 * {@link ILoadHandler#onLoading(ImageTask)} will be called when begin to load the image.
	 * </p>
	 * 
	 * <p>
	 * {@link ILoadHandler#onLoadFinish(ImageTask, BitmapDrawable)} will be called after the image is loaded.
	 * </p>
	 */
	public interface ILoadHandler {

		/**
		 * When begin to load the image from disk or network.
		 */
		public void onLoading(ImageTask imageTask);

		/**
		 * After image is loaded.
		 */
		public void onLoadFinish(ImageTask imageTask, BitmapDrawable drawable);
	}

	/**
	 * A Worker can do a work in a background thread and cacel the work if necessary.
	 * 
	 * You can implements A worker who can excute work using multiple thread, or simpley use the {@link DefaultWorker}.
	 */
	public interface IWorker {

		/**
		 * Do the work in a background thread.
		 */
		public void doWork(BaseWork work);

		/**
		 * Cancel the work.
		 */
		public void cancleWork(BaseWork work);
	}

	/**
	 * A ImageResizer process the resize logical when loading image from network an disk.
	 */
	public interface IImageResizer {

		/**
		 * Return the {@link BitmapFactory.Options#inSampleSize}, which will be used when load the image from the disk.
		 * 
		 * You should better calacute this value accroiding the hard device of the mobile.
		 */
		public int getInSampleSize(ImageTask imageTask);

		/**
		 * If you have a thrumb web service which can return multiple size image acrroding the url,
		 * 
		 * you can implements this method to return the specified url accoding the request size.
		 */
		public String getResizedUrl(ImageTask imageTask);
	}

	private static final boolean DEBUG = true;
	private static final String Log_TAG = "cube_image";

	protected IWorker mWorker;
	protected IImageResizer mResizer;
	protected ImageProvider mImageProvider;

	protected boolean mPauseWork = false;
	protected boolean mExitTasksEarly = false;

	private final Object mPauseWorkLock = new Object();
	private HashMap<String, LoadImageWork> mLoadWorkList;

	public Resources mResources;

	public ImageLoader(Context context, ImageProvider imageProvider, IWorker worker, IImageResizer imageResizer) {
		mResources = context.getResources();
		mWorker = worker;
		mResizer = imageResizer;
		mImageProvider = imageProvider;
		mLoadWorkList = new HashMap<String, LoadImageWork>();
	}

	/**
	 * Process the ImageTask.
	 */
	public void processImageTask(ImageTask imageTask) {

		synchronized (mPauseWorkLock) {
			processImageTaskInner(imageTask);
		}
	}

	private void processImageTaskInner(ImageTask imageTask) {

		ImageView imageView = imageTask.getWeakReferenceImageView();

		// 1. If imageview is null, this task may be running for loading the image data in advance,
		// else cancel the poential ImageTask which is realted the this ImageView previous and still running.
		if (imageView != null) {

			cancelPotentialWork(imageView, imageTask);

			// 4. relate current ImageTask to this imageView;
			ImageViewBadge.setBadage(imageView, imageTask);

			BitmapDrawable drawable = null;
			if (mImageProvider != null) {
				drawable = mImageProvider.getBitmapFromMemCache(imageTask);
			}

			// memory cache is hit, return at once.
			if (drawable != null) {
				imageTask.onLoadFinish(drawable);

				if (DEBUG) {
					Log.d(Log_TAG, String.format("%s memory cache is hit, return at once.", imageTask));
				}
				return;
			}
		}

		// 2. Check the if the corresponding LoadImageWork is runing.
		LoadImageWork runningLoadImageWork = mLoadWorkList.get(imageTask.getIdentityKey());

		// 3. If really has the running LoadImageWork existed, set the imageView to the previous ImageTask.
		// Once the LoadImageWork is done, we can call mImageTask.getImageView() in onLoadFinish method to get this imageView.
		if (runningLoadImageWork != null) {
			runningLoadImageWork.getImageTask().setRelatedImageView(imageView);
		}
		// 4. run another LoadImageWork to load image.
		else {

			LoadImageWork work = new LoadImageWork(imageTask);
			imageTask.onLoading();
			ImageViewBadge.setBadage(imageView, imageTask);
			mLoadWorkList.put(imageTask.getIdentityKey(), work);

			mWorker.doWork(work);
		}
	}

	private boolean cancelPotentialWork(ImageView imageView, ImageTask imageTask) {
		// 1. check if the imageview has a related ImageTask.
		ImageTask hodingImageTask = ImageViewBadge.getBadge(imageView);

		// 2. check if the holding ImageTask is not the same with this one.
		if (hodingImageTask != null) {

			// 3.1 not the same with the current one, cancle the previous.
			if (hodingImageTask != imageTask) {
				String holdingImageTaskKey = hodingImageTask.getIdentityKey();
				LoadImageWork work = mLoadWorkList.get(holdingImageTaskKey);
				if (work != null) {

					if (DEBUG) {
						Log.d(Log_TAG, String.format("%s imageview is reused, the previous work is cancled.", imageTask));
					}
					mWorker.cancleWork(work);
					mLoadWorkList.remove(holdingImageTaskKey);
					work = null;
				}
			}
			// 3.2 The holding task is the same with the current one, but do nothing here.
			// Because the flowing code in the place where call this method will check again.
			else {
				if (DEBUG) {
					Log.d(Log_TAG, String.format("%s The same work is already in progress.", imageTask));
				}
			}
		}
		return false;
	}

	private class LoadImageWork extends BaseWork {

		private ImageTask mImageTask;
		private BitmapDrawable drawable;

		public LoadImageWork(ImageTask imageTask) {
			this.mImageTask = imageTask;
		}

		public ImageTask getImageTask() {
			return mImageTask;
		}

		@Override
		public void doInBackground() {
			Bitmap bitmap = null;

			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						if (DEBUG) {
							Log.d(Log_TAG, String.format("%s wait. isCancelled: %s mPauseWork: %s", mImageTask, isCancelled(), mPauseWork));
						}
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			// If this task has not been cancelled by another
			// thread and the ImageView that was originally bound to this task is still bound back
			// to this task and our "exit early" flag is not set then try and fetch the bitmap from
			// the cache
			if (!isCancelled() && !mExitTasksEarly && stillAttachingTheSameImageViewOrJustForPreLoad()) {
				bitmap = mImageProvider.fetchBitmapData(mImageTask, mResizer);
				drawable = mImageProvider.createBitmapDrawable(mResources, bitmap);
				mImageProvider.addBitmapToMemCache(mImageTask.getIdentityKey(), drawable);
			}
		}

		/**
		 * Check if the ImageView associated with this task as long as the ImageView's task still points to this task as well.
		 * 
		 * IN THE SAME TIME: If the ImageView which potints to this task is reused for another task. We will cancel this LoadImageWork.
		 */
		private boolean stillAttachingTheSameImageViewOrJustForPreLoad() {
			final ImageView imageView = mImageTask.getWeakReferenceImageView();
			if (imageView == null) {
				return true;
			}
			final ImageTask iamgeTask = ImageViewBadge.getBadge(imageView);
			if (this.mImageTask == iamgeTask) {
				return true;
			}
			return false;
		}

		@Override
		public void onPostExecute() {
			if (stillAttachingTheSameImageViewOrJustForPreLoad()) {
				mImageTask.onLoadFinish(drawable);
			}
			mLoadWorkList.remove(mImageTask.getIdentityKey());
		}
	}

	/**
	 * Temporarily hand up work, you can call this when the view is scrolling.
	 */
	public void pauseWork() {
		mPauseWork = true;
		mExitTasksEarly = false;
	}

	/**
	 * Resume the work
	 */
	public void resumeWork() {
		synchronized (mPauseWorkLock) {
			mPauseWork = false;
			mExitTasksEarly = false;
			mPauseWorkLock.notifyAll();
		}
	}

	public void clearCache() {
		mImageProvider.clearCache();
	}

	public void stopWork() {
		mPauseWork = true;
		mExitTasksEarly = true;
	}

	public void destory() {
		mPauseWork = true;
		mExitTasksEarly = true;
		// remove all the running task
	}
}
