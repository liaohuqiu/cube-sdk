package com.srain.cube.image;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.srain.cube.concurrent.SimpleTask;
import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.image.iface.ImageResizer;
import com.srain.cube.image.imple.DefaultExecutor;
import com.srain.cube.image.imple.DefaultImageLoadHandler;
import com.srain.cube.image.imple.DefaultResizer;
import com.srain.cube.util.CLog;

public class ImageLoader {

	private static final String MSG_DUPLICATED = "%s duplicated";
	private static final String MSG_BEGIN_PROCESS = "%s processImageTaskInner %s";
	private static final String MSG_HAS_PRE_TASK = "%s reused, disconnect from previous one.";
	private static final String MSG_ATTACK_TO_RUNNING_TASK = "%s attach to running: %s";

	private static final String MSG_TASK_DO_IN_BACKGROUND = "%s doInBackground";
	private static final String MSG_TASK_FINISH = "%s onFinish";
	private static final String MSG_TASK_CANCEL = "%s onCancel";

	protected static final boolean DEBUG = CLog.DEBUG;
	protected static final String Log_TAG = "cube_image";

	protected Executor mLoadImgageExcutor;
	protected ImageResizer mResizer;
	protected ImageProvider mImageProvider;
	protected ImageLoadHandler mImageLoadHandler;

	protected boolean mPauseWork = false;
	protected boolean mExitTasksEarly = false;

	private final Object mPauseWorkLock = new Object();
	private HashMap<String, LoadImageTask> mLoadWorkList;
	protected Context mContext;

	protected Resources mResources;

	public ImageLoader(Context context, ImageProvider imageProvider, Executor executor, ImageResizer imageResizer, ImageLoadHandler imageLoadHandler) {
		mContext = context;
		mResources = context.getResources();

		if (imageProvider == null) {
			imageProvider = ImageProvider.getDefault(context);
		}
		mImageProvider = imageProvider;

		if (executor == null) {
			executor = DefaultExecutor.getInstance();
		}
		mLoadImgageExcutor = executor;

		if (imageResizer == null) {
			imageResizer = DefaultResizer.getInstance();
		}
		mResizer = imageResizer;

		if (imageLoadHandler == null) {
			imageLoadHandler = new DefaultImageLoadHandler(context);
		}
		mImageLoadHandler = imageLoadHandler;

		mLoadWorkList = new HashMap<String, LoadImageTask>();
	}

	public static ImageLoader createDefault(Context context) {
		DefaultImageLoadHandler imageLoadHandler = new DefaultImageLoadHandler(context);
		return new ImageLoader(context, ImageProvider.getDefault(context), DefaultExecutor.getInstance(), DefaultResizer.getInstance(), imageLoadHandler);
	}

	public void setImageLoadHandler(ImageLoadHandler imageLoadHandler) {
		mImageLoadHandler = imageLoadHandler;
	}

	/**
	 * Load the image in advance.
	 */
	public void preLoadImages(String[] urls) {
		int len = urls.length;
		len = 10;
		for (int i = 0; i < len; i++) {
			final ImageTask imageTask = new ImageTask(urls[i], 0, 0, null);
			processImageTask(imageTask, null);
		}
	}

	/**
	 * Load image.
	 */
	public void loadImage(CubeImageView imageView, String url, int requestWidth, int requestHeight, ImageReuseInfo imageReuseInfo) {
		final ImageTask imageTask = new ImageTask(url, requestWidth, requestWidth, imageReuseInfo);
		processImageTaskInner(imageTask, imageView);
	}

	/**
	 * Process the ImageTask.
	 */
	public void processImageTask(ImageTask imageTask, CubeImageView imageView) {

		synchronized (mPauseWorkLock) {
			processImageTaskInner(imageTask, imageView);
		}
	}

	private void processImageTaskInner(ImageTask imageTask, CubeImageView imageView) {

		ImageTask hodingImageTask = imageView.getHoldingImageTask();

		// 1. Check the previous ImageTask related to this ImageView
		if (hodingImageTask != null) {

			// duplicated ImageTask, return directly.
			if (hodingImageTask.equals(imageTask)) {
				if (imageView.getDrawable() != null) {
					if (DEBUG) {
						Log.d(Log_TAG, String.format(MSG_DUPLICATED, hodingImageTask));
					}
					return;
				}
			}
			// ImageView is reused, from it from the related ImageViews of the previous ImageTask.
			else {
				if (!hodingImageTask.isDoneOrAborted()) {
					if (DEBUG) {
						Log.d(Log_TAG, String.format(MSG_HAS_PRE_TASK, imageTask));
					}
					hodingImageTask.removeRelatedImageView(imageView);
					if (!hodingImageTask.isPreLoad() && !hodingImageTask.stillHasRelatedImageView()) {
						LoadImageTask task = mLoadWorkList.get(hodingImageTask.getIdentityKey());
						if (task != null) {
							task.cancel(true);
						}
						if (DEBUG) {
							Log.d(Log_TAG, String.format("%s previous work is cancelled.", hodingImageTask));
						}
					}
				}
			}
		}

		if (DEBUG) {
			Log.d(Log_TAG, String.format(MSG_BEGIN_PROCESS, imageTask, hodingImageTask));
		}

		// 2. Let the ImageView hold this ImageTask. When ImageView is reused next time, check it in step 1.
		if (imageView != null) {
			imageView.setHoldingImageTask(imageTask);
		}

		// 3. Make the ImageView related to this ImageTask or the previous running one.
		LoadImageTask runningLoadImageWork = mLoadWorkList.get(imageTask.getIdentityKey());
		if (runningLoadImageWork != null) {
			if (imageView != null) {
				if (DEBUG) {
					Log.d(Log_TAG, String.format(MSG_ATTACK_TO_RUNNING_TASK, imageTask, runningLoadImageWork.getImageTask()));
				}
				runningLoadImageWork.getImageTask().addRelatedImageView(imageView, mImageLoadHandler);
			}
			return;
		} else {
			imageTask.addRelatedImageView(imageView, mImageLoadHandler);
		}

		LoadImageTask work = new LoadImageTask(imageTask);
		mLoadWorkList.put(imageTask.getIdentityKey(), work);
		imageTask.onLoading(mImageLoadHandler);
		mLoadImgageExcutor.execute(work);
	}

	private class LoadImageTask extends SimpleTask {

		private ImageTask mImageTask;
		private BitmapDrawable mDrawable;

		public LoadImageTask(ImageTask imageTask) {
			this.mImageTask = imageTask;
		}

		public ImageTask getImageTask() {
			return mImageTask;
		}

		@Override
		public void doInBackground() {
			if (DEBUG) {
				Log.d(Log_TAG, String.format(MSG_TASK_DO_IN_BACKGROUND, mImageTask));
			}
			Bitmap bitmap = null;
			// Wait here if work is paused and the task is not cancelled
			synchronized (mPauseWorkLock) {
				while (mPauseWork && !isCancelled()) {
					try {
						if (DEBUG) {
							Log.d(Log_TAG, String.format("%s wait to begin", mImageTask));
						}
						mPauseWorkLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}

			if (!isCancelled() && !mExitTasksEarly && (mImageTask.isPreLoad() || mImageTask.stillHasRelatedImageView())) {
				if (mImageProvider != null) {
					mDrawable = mImageProvider.getBitmapFromMemCache(mImageTask);
				}
				// memory cache is hit, return at once.
				if (mDrawable != null) {
					return;
				}
			}

			// If this task has not been cancelled by another
			// thread and the ImageView that was originally bound to this task is still bound back
			// to this task and our "exit early" flag is not set then try and fetch the bitmap from
			// the cache
			if (!isCancelled() && !mExitTasksEarly && (mImageTask.isPreLoad() || mImageTask.stillHasRelatedImageView())) {
				try {
					bitmap = mImageProvider.fetchBitmapData(mImageTask, mResizer);
					mDrawable = mImageProvider.createBitmapDrawable(mResources, bitmap);
					mImageProvider.addBitmapToMemCache(mImageTask.getIdentityKey(), mDrawable);
				} catch (Exception e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onFinish() {
			if (DEBUG) {
				Log.d(Log_TAG, String.format(MSG_TASK_FINISH, mImageTask));
			}
			if (mExitTasksEarly) {
				return;
			}
			mLoadWorkList.remove(mImageTask.getIdentityKey());

			if (!isCancelled() && !mExitTasksEarly) {
				mImageTask.onLoadFinish(mDrawable, mImageLoadHandler);
			}
		}

		@Override
		public void onCancel() {
			if (DEBUG) {
				Log.d(Log_TAG, String.format(MSG_TASK_CANCEL, mImageTask));
			}
			mLoadWorkList.remove(mImageTask.getIdentityKey());
			mImageTask.onCancel();
		}
	}

	private void setPause(boolean pause) {
		synchronized (mPauseWorkLock) {
			mPauseWork = pause;
			if (!pause) {
				mPauseWorkLock.notifyAll();
			}
		}
	}

	/**
	 * Temporarily hand up work, you can call this when the view is scrolling.
	 */
	public void pauseWork() {
		mExitTasksEarly = false;
		setPause(true);
		if (DEBUG) {
			Log.d(Log_TAG, String.format("work_status: pauseWork %s", this));
		}
	}

	/**
	 * Resume the work
	 */
	public void resumeWork() {
		mExitTasksEarly = false;
		setPause(false);
		if (DEBUG) {
			Log.d(Log_TAG, String.format("work_status: resumeWork %s", this));
		}
	}

	/**
	 * Recover the from the work list
	 */
	public void recoverWork() {
		if (DEBUG) {
			Log.d(Log_TAG, String.format("work_status: recoverWork %s", this));
		}
		mExitTasksEarly = false;
		setPause(false);
		Iterator<Entry<String, LoadImageTask>> it = (Iterator<Entry<String, LoadImageTask>>) mLoadWorkList.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, LoadImageTask> item = it.next();
			LoadImageTask task = item.getValue();
			task.restart();
			mLoadImgageExcutor.execute(task);
		}
	}

	/**
	 * Drop all the work, and leave it in the work list.
	 */
	public void stopWork() {
		if (DEBUG) {
			Log.d(Log_TAG, String.format("work_status: stopWork %s", this));
		}
		mExitTasksEarly = true;
		setPause(false);
	}

	/**
	 * Drop all the work, clear the work list.
	 */
	public void destory() {
		mExitTasksEarly = true;
		setPause(false);

		Iterator<Entry<String, LoadImageTask>> it = (Iterator<Entry<String, LoadImageTask>>) mLoadWorkList.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, LoadImageTask> item = it.next();
			final LoadImageTask task = item.getValue();
			it.remove();
			if (task != null) {
				task.cancel(true);
			}
		}
		mLoadWorkList.clear();

	}

	public ImageProvider getImageProvider() {
		return mImageProvider;
	}
}
