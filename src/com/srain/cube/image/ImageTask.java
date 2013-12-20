package com.srain.cube.image;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.srain.cube.image.ImageLoader.ILoadHandler;
import com.srain.cube.util.Encrypt;

import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * A wrapper of the related information used in loading a bitmap
 * 
 * @author huqiu.lhq
 */
public class ImageTask {

	private String mUrl;
	private WeakReference<ImageView> mImageViewReference;

	private int mOriginWidth = 0;
	private int mOriginHeight = 0;
	private int mRequestWidth = 0;
	private int mRequestHeight = 0;

	private ArrayList<Point> mSizes = new ArrayList<Point>();

	private ILoadHandler mLoadHandler;

	public ImageTask(String url) {
		this(url, 0);
	}

	public ImageTask(String url, int requestSize) {
		this(url, requestSize, requestSize);
	}

	public ImageTask(String url, int requestWidth, int requestHeight) {
		mUrl = url;
		mRequestWidth = requestWidth;
		mRequestHeight = requestHeight;
		mSizes.add(new Point());
	}

	public void setRelatedImageView(ImageView imageView) {
		mImageViewReference = new WeakReference<ImageView>(imageView);
	}

	public void setLoadHandler(ILoadHandler handler) {
		mLoadHandler = handler;
	}

	public void onLoading() {
		if (null != mLoadHandler) {
			mLoadHandler.onLoading(this);
		}
	}

	/**
	 * Will be called when begin load image data from dish or network
	 * 
	 * @param drawable
	 */
	public void onLoadFinish(BitmapDrawable drawable) {
		if (null != mLoadHandler) {
			mLoadHandler.onLoadFinish(this, drawable);
		}
	}

	public ImageView getWeakReferenceImageView() {
		if (null == mImageViewReference) {
			return null;
		}
		return mImageViewReference.get();
	}

	public String getRemoteUrl() {
		return mUrl;
	}

	public void setOriginSize(int width, int height) {
		mOriginWidth = width;
		mOriginHeight = height;
	}

	public int getOriginWidth() {
		return mOriginWidth;
	}

	public int getOriginHeight() {
		return mOriginHeight;
	}

	public int getRequestWidth() {
		return mRequestWidth;
	}

	public int getRequestHeight() {
		return mRequestHeight;
	}

	/**
	 * Return the key which identifies this Image Wrapper object.
	 */
	public String getIdentityKey() {
		return genIdentityKey(mUrl, mRequestWidth, mRequestHeight);
	}

	private static String genIdentityKey(String key, int w, int h) {
		if (w > 0 && h > 0) {
			key += w + "_" + h;
		}
		return Encrypt.md5(key);
	}

	/**
	 * Self size will be last, (0, 0) is the default, design for preload.
	 */
	public ArrayList<String> getDiskCacheKeys() {
		ArrayList<String> keys = new ArrayList<String>();
		for (int i = 0; i < mSizes.size(); i++) {
			Point p = mSizes.get(i);
			keys.add(genIdentityKey(mUrl, p.x, p.y));
		}

		if (mRequestWidth > 0 && mRequestHeight > 0) {
			keys.add(genIdentityKey(mUrl, mRequestWidth, mRequestHeight));
		}
		return keys;
	}

	public boolean equals(Object object) {
		if (object != null && object instanceof ImageTask) {
			return ((ImageTask) object).getIdentityKey() == getIdentityKey();
		}
		return false;
	}

	@Override
	public String toString() {
		return mUrl;
	}
}
