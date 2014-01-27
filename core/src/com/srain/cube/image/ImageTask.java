package com.srain.cube.image;

import java.lang.ref.WeakReference;

import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.SparseArray;

import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.util.Encrypt;

/**
 * A wrapper of the related information used in loading a bitmap
 * 
 * @author huqiu.lhq
 */
public class ImageTask {

	private static int sId = 0;;
	protected int mId;

	private static final String SIZE_SP = "_";

	protected String mUrl;

	protected Point mOriginSize = new Point();
	private Point mRequestSize = new Point();

	protected boolean mIsPreLoad = false;
	protected boolean mIsDoneOrAborted = false;
	protected boolean mIsLoading = false;

	protected SparseArray<String> mReuseCacheKeys = new SparseArray<String>();

	private String mIndentityKey;

	protected ImageReuseInfo mImageReuseInfo;

	private ImageViewHolder mImageViewHolder;

	public ImageTask(String url, int requestWidth, int requestHeight, ImageReuseInfo imageReuseInfo) {

		mId = ++sId;

		mUrl = url;
		if (imageReuseInfo != null) {
			mImageReuseInfo = imageReuseInfo;
		}
		mRequestSize = new Point(requestWidth, requestHeight);
		mIndentityKey = genSizeKey(mUrl, mRequestSize.x, mRequestSize.y);
	}

	public boolean isPreLoad() {
		return mIsPreLoad;
	}

	public void setPreLoad(boolean preload) {
		mIsPreLoad = preload;
	}

	public boolean isDoneOrAborted() {
		return mIsDoneOrAborted;
	}

	public void addImageView(CubeImageView imageView) {
		if (null == imageView) {
			return;
		}
		if (null == mImageViewHolder) {
			mImageViewHolder = new ImageViewHolder(imageView);
			return;
		}

		ImageViewHolder holder = mImageViewHolder;
		for (;; holder = holder.next()) {
			if (holder.contains(imageView)) {
				return;
			}
			if (!holder.hasNext()) {
				break;
			}
		}

		ImageViewHolder newHolder = new ImageViewHolder(imageView);
		newHolder.mPrev = holder;
		holder.mNext = newHolder;
	}

	public void removeImageView(CubeImageView imageView) {

		if (null == imageView || null == mImageViewHolder) {
			return;
		}

		ImageViewHolder holder = mImageViewHolder;

		do {
			if (holder.contains(imageView)) {

				if (holder == mImageViewHolder) {
					mImageViewHolder = holder.mNext;
				}
				if (null != holder.mNext) {
					holder.mNext.mPrev = holder.mPrev;
				}
				if (null != holder.mPrev) {
					holder.mPrev.mNext = holder.mNext;
				}
			}

		} while ((holder = holder.next()) != null);
	}

	public boolean stillHasRelatedImageView() {
		if (null == mImageViewHolder || mImageViewHolder.getImageView() == null) {
			return false;
		} else {
			return true;
		}
	}

	public void onLoading(ImageLoadHandler handler) {
		mIsLoading = true;

		if (null == handler || null == mImageViewHolder) {
			return;
		}

		ImageViewHolder holder = mImageViewHolder;
		do {
			final CubeImageView imageView = holder.getImageView();
			if (null != imageView) {
				handler.onLoading(this, imageView);
			}
		} while ((holder = holder.next()) != null);
	}

	/**
	 * Will be called when begin load image data from dish or network
	 * 
	 * @param drawable
	 */
	public void onLoadFinish(BitmapDrawable drawable, ImageLoadHandler handler) {
		mIsLoading = false;
		mIsDoneOrAborted = true;

		if (null == handler || null == mImageViewHolder) {
			return;
		}

		ImageViewHolder holder = mImageViewHolder;
		do {
			final CubeImageView imageView = holder.getImageView();
			if (null != imageView) {
				handler.onLoadFinish(this, imageView, drawable);
			}
		} while ((holder = holder.next()) != null);
	}

	public void onCancel() {
		mIsLoading = false;
		mIsDoneOrAborted = true;
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

	private String mStr;

	@Override
	public String toString() {
		if (mStr == null) {
			mStr = String.format("%s %sx%s", mId, mRequestSize.x, mRequestSize.y);
		}
		return mStr;
	}

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

		ImageViewHolder next() {
			return mNext;
		}

		boolean hasNext() {
			return mNext != null;
		}
	}
}
