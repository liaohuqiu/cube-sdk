package com.srain.cube.image;

import java.lang.ref.WeakReference;

import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.ImageView.ScaleType;

import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.util.Encrypt;

/**
 * A wrapper of the related information used in loading a bitmap
 * 
 * @author huqiu.lhq
 */
public class ImageTask {

	private static int sId = 0;;

	private int mFlag;
	protected int mId;
	protected String mUrl;

	private Point mRequestSize = new Point();
	protected Point mOriginSize = new Point();

	private static final String SIZE_SP = "_";
	private final static int STATUS_PRE_LOAD = 0x01;
	private final static int STATUS_LOADING = 0x02;
	private final static int STATUS_DONE = 0x04;
	private final static int STATUS_CANCELED = 0x08;

	protected SparseArray<String> mReuseCacheKeys = new SparseArray<String>();

	private String mIndentityKey;
	private String mStr;

	private ImageViewHolder mFirstImageViewHolder;
	protected ImageReuseInfo mImageReuseInfo;
	protected ScaleType mScaleType;
	protected boolean mAdjustBounds = false;

	public ImageTask(String url, int requestWidth, int requestHeight, ImageReuseInfo imageReuseInfo) {

		mId = ++sId;

		mUrl = url;
		if (imageReuseInfo != null) {
			mImageReuseInfo = imageReuseInfo;
		}
		mRequestSize = new Point(requestWidth, requestHeight);
		mIndentityKey = genSizeKey(mUrl, mRequestSize.x, mRequestSize.y);
	}

	public void setCacleType(ScaleType scaleType) {
		mScaleType = scaleType;
	}

	public ScaleType getScaleType() {
		return mScaleType;
	}

	public boolean isAdjustBounds() {
		return mAdjustBounds;
	}

	public void setAdjustBound(boolean adjust) {
		mAdjustBounds = adjust;
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

	public CubeImageView getAImageView() {
		ImageViewHolder holder = mFirstImageViewHolder;

		CubeImageView imageView = null;
		do {
			if ((imageView = holder.getImageView()) != null) {
				return imageView;
			}
		} while ((holder = holder.mNext) != null);
		return null;
	}

	public void onCancel() {
		mFlag &= ~STATUS_LOADING;
		mFlag |= STATUS_CANCELED;
	}

	public String getRemoteUrl() {
		return mUrl;
	}

	public void setOriginSize(int width, int height) {
		mOriginSize = new Point(width, height);
	}

	public Point getOriginSize() {
		return mOriginSize;
	}

	public Point getRequestSize() {
		return mRequestSize;
	}

	/**
	 * Return the key which identifies this Image Wrapper object.
	 */
	public String getIdentityKey() {
		return mIndentityKey;
	}

	protected static String genSizeKey(String key, int w, int h) {
		if (w > 0 && h != Integer.MAX_VALUE && h > 0 && h != Integer.MAX_VALUE) {
			return new StringBuilder(key).append(SIZE_SP).append(w).append(SIZE_SP).append(h).toString();
		}
		return key;
	}

	public String genFileCacheKey(String sizeTag) {
		if (TextUtils.isEmpty(sizeTag)) {
			return Encrypt.md5(mUrl);
		} else {
			return Encrypt.md5(new StringBuilder(mUrl).append(SIZE_SP).append(sizeTag).toString());
		}
	}

	public ImageReuseInfo getImageReuseInfo() {
		return mImageReuseInfo;
	}

	public boolean equals(Object object) {
		if (object != null && object instanceof ImageTask) {
			return ((ImageTask) object).getIdentityKey().equals(getIdentityKey());
		}
		return false;
	}

	@Override
	public String toString() {
		if (mStr == null) {
			mStr = String.format("%s %sx%s", mId, mRequestSize.x, mRequestSize.y);
		}
		return mStr;
	}

	/**
	 * A tiny and light linked list like container to hold all the ImageView related to ImageTask
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
