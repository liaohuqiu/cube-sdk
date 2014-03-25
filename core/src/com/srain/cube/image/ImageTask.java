package com.srain.cube.image;

import java.lang.ref.WeakReference;

import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;

import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.util.Encrypt;

/**
 * A wrapper of the related information used in loading a bitmap
 * 
 * @author http://www.liaohuqiu.net
 */
public class ImageTask {

	private static int sId = 0;;

	private int mFlag;
	protected int mId;
	protected String mOriginUrl;

	protected Point mRequestSize = new Point();
	protected Point mBitmapOriginSize = new Point();

	private final static String SIZE_SP = "_";
	private final static int STATUS_PRE_LOAD = 0x01;
	private final static int STATUS_LOADING = 0x02;
	private final static int STATUS_DONE = 0x04;
	private final static int STATUS_CANCELED = 0x08;

	private String mIdentityKey;
	private String mStr;

	private ImageViewHolder mFirstImageViewHolder;
	protected ImageReuseInfo mImageReuseInfo;

	public ImageTask(String originUrl, int requestWidth, int requestHeight, ImageReuseInfo imageReuseInfo) {

		mId = ++sId;

		mOriginUrl = originUrl;
		mRequestSize = new Point(requestWidth, requestHeight);
		if (imageReuseInfo != null) {
			mImageReuseInfo = imageReuseInfo;
		}
	}

	/**
	 * Generate the identity key.
	 * 
	 * @return
	 */
	protected String genIdentityKey() {
		return joinSizeInfo(mOriginUrl, mRequestSize.x, mRequestSize.y);
	}

	public boolean isPreLoad() {
		return (mFlag & STATUS_PRE_LOAD) == STATUS_PRE_LOAD;
	}

	public void setPreLoad(boolean preload) {
		mFlag = mFlag | STATUS_PRE_LOAD;
	}

	public boolean isLoading() {
		return (mFlag & STATUS_LOADING) != 0;
	}

	public boolean isLoadingThisUrl(String url) {
		return mOriginUrl.equals(url);
	}

	/**
	 * Bind ImageView with ImageTask
	 * 
	 * @param imageView
	 */
	public void addImageView(CubeImageView imageView) {
		if (null == imageView) {
			return;
		}
		if (null == mFirstImageViewHolder) {
			mFirstImageViewHolder = new ImageViewHolder(imageView);
			return;
		}

		ImageViewHolder holder = mFirstImageViewHolder;
		for (;; holder = holder.mNext) {
			if (holder.contains(imageView)) {
				return;
			}
			if (holder.mNext == null) {
				break;
			}
		}

		ImageViewHolder newHolder = new ImageViewHolder(imageView);
		newHolder.mPrev = holder;
		holder.mNext = newHolder;
	}

	/**
	 * Remove the ImageView from ImageTask
	 * 
	 * @param imageView
	 */
	public void removeImageView(CubeImageView imageView) {

		if (null == imageView || null == mFirstImageViewHolder) {
			return;
		}

		ImageViewHolder holder = mFirstImageViewHolder;

		do {
			if (holder.contains(imageView)) {

				// Make sure entry is right.
				if (holder == mFirstImageViewHolder) {
					mFirstImageViewHolder = holder.mNext;
				}
				if (null != holder.mNext) {
					holder.mNext.mPrev = holder.mPrev;
				}
				if (null != holder.mPrev) {
					holder.mPrev.mNext = holder.mNext;
				}
			}

		} while ((holder = holder.mNext) != null);
	}

	/**
	 * Check if this ImageTask has any related ImageViews.
	 * 
	 * @return
	 */
	public boolean stillHasRelatedImageView() {
		if (null == mFirstImageViewHolder || mFirstImageViewHolder.getImageView() == null) {
			return false;
		} else {
			return true;
		}
	}

	public void onLoading(ImageLoadHandler handler) {
		mFlag = mFlag | STATUS_LOADING;

		if (null == handler || null == mFirstImageViewHolder) {
			return;
		}

		ImageViewHolder holder = mFirstImageViewHolder;
		do {
			final CubeImageView imageView = holder.getImageView();
			if (null != imageView) {
				handler.onLoading(this, imageView);
			}
		} while ((holder = holder.mNext) != null);
	}

	/**
	 * Will be called when begin load image data from dish or network
	 * 
	 * @param drawable
	 */
	public void onLoadFinish(BitmapDrawable drawable, ImageLoadHandler handler) {

		mFlag &= ~STATUS_LOADING;
		mFlag |= STATUS_DONE;

		if (null == handler || null == mFirstImageViewHolder) {
			return;
		}

		ImageViewHolder holder = mFirstImageViewHolder;
		do {
			final CubeImageView imageView = holder.getImageView();
			if (null != imageView) {
				imageView.onLoadFinish();
				handler.onLoadFinish(this, imageView, drawable);
			}
		} while ((holder = holder.mNext) != null);
	}

	public void onCancel() {
		mFlag &= ~STATUS_LOADING;
		mFlag |= STATUS_CANCELED;
	}

	/**
	 * If you have a thumbnail web service which can return multiple size image according the url,
	 * 
	 * you can implements this method to return the specified url according the request size.
	 * 
	 * @return
	 */
	public String getRemoteUrl() {
		return mOriginUrl;
	}

	/**
	 * Return the origin request url
	 * 
	 * @return
	 */
	public String getOriginUrl() {
		return mOriginUrl;
	}

	public void setBitmapOriginSize(int width, int height) {
		mBitmapOriginSize = new Point(width, height);
	}

	public Point getBitmapOriginSize() {
		return mBitmapOriginSize;
	}

	public Point getRequestSize() {
		return mRequestSize;
	}

	/**
	 * Return the key which identifies this Image Wrapper object.
	 */
	public String getIdentityKey() {
		if (mIdentityKey == null) {
			mIdentityKey = genIdentityKey();
		}
		return mIdentityKey;
	}

	/**
	 * Join the key and the size information.
	 * 
	 * @param key
	 * @param w
	 * @param h
	 * @return
	 */
	public static String joinSizeInfo(String key, int w, int h) {
		if (w > 0 && h != Integer.MAX_VALUE && h > 0 && h != Integer.MAX_VALUE) {
			return new StringBuilder(key).append(SIZE_SP).append(w).append(SIZE_SP).append(h).toString();
		}
		return key;
	}

	/**
	 * Generate the file cache key, just using the md5 algorithm.
	 * 
	 * @param key
	 * @param part2
	 * @return
	 */
	public static String genFileCacheKey(String key, String part2) {
		if (TextUtils.isEmpty(part2)) {
			return Encrypt.md5(key);
		} else {
			return Encrypt.md5(new StringBuilder(key).append(SIZE_SP).append(part2).toString());
		}
	}

	/**
	 * Return the cache key for file cache.
	 * 
	 * @return
	 */
	public String getFileCacheKey() {
		if (mImageReuseInfo != null) {
			return genFileCacheKey(mOriginUrl, mImageReuseInfo.getIdentitySize());
		} else {
			return genFileCacheKey(getIdentityKey(), null);
		}
	}

	public ImageReuseInfo getImageReuseInfo() {
		return mImageReuseInfo;
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof ImageTask) {
			return ((ImageTask) object).getIdentityKey().equals(getIdentityKey());
		}
		return false;
	}

	@Override
	public String toString() {
		if (mStr == null) {
			mStr = String.format("%s %sx%s %s", mId, mRequestSize.x, mRequestSize.y, getIdentityKey());
		}
		return mStr;
	}

	/**
	 * A tiny and light linked-list like container to hold all the ImageViews related to the ImageTask.
	 */
	private static class ImageViewHolder {
		private WeakReference<CubeImageView> mImageViewRef;
		private ImageViewHolder mNext;
		private ImageViewHolder mPrev;

		public ImageViewHolder(CubeImageView imageView) {
			mImageViewRef = new WeakReference<CubeImageView>(imageView);
		}

		boolean contains(CubeImageView imageView) {
			return mImageViewRef != null && imageView == mImageViewRef.get();
		}

		CubeImageView getImageView() {
			if (null == mImageViewRef) {
				return null;
			}
			return mImageViewRef.get();
		}
	}
}
