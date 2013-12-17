package com.srain.sdk.image;

import android.graphics.BitmapFactory;
import android.util.Log;

import com.srain.sdk.util.Encrypt;

public class ImageRequest {

	private String mUrl;

	private int mOriginWidth = 0;
	private int mOriginHeight = 0;
	private int mRequestWidth = Integer.MAX_VALUE;
	private int mRequestHeight = Integer.MAX_VALUE;

	private int mDebugIndex = 0;

	public ImageRequest(String url) {
		mUrl = url;
	}

	public void setDebugIndex(int index) {
		mDebugIndex = index;
	}

	public String toString() {
		if (mOriginWidth == 0) {
			return String.valueOf(mDebugIndex);
		}
		return String.format("%s, %sx%s => %sx%s", mDebugIndex, mOriginWidth, mOriginHeight, mRequestWidth, mRequestWidth);
	}

	public String getRemoteUrl() {
		return mUrl;
	}

	public int getRequestWidth() {
		return mRequestWidth;
	}

	public int getRequestHeight() {
		return mRequestHeight;
	}

	public void setSize(int size) {
		setSize(size, size);
	}

	public void setSize(int requestWidth, int requestHeight) {
		mRequestWidth = requestWidth;
		mRequestHeight = requestHeight;
	}

	public String getCacheKey() {
		return Encrypt.md5(mUrl);
	}

	public int getInSampleSize(BitmapFactory.Options options) {
		mOriginHeight = options.outHeight;
		mOriginWidth = options.outWidth;
		int size = calculateInSampleSize(mOriginWidth, mOriginHeight, mRequestWidth, mRequestWidth);

		Log.d("cube_image", String.format("%s, getInSampleSize: %s", size, this));
		return size;
	}

	/**
	 * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap having a width and height equal to or larger than the requested width and height.
	 * 
	 * @param options
	 *            An options object with out* params already populated (run through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(int originWidth, int originHeight, int reqWidth, int reqHeight) {

		int inSampleSize = 1;

		if (originWidth > reqHeight || originHeight > reqWidth) {

			final int halfHeight = originWidth / 2;
			final int halfWidth = originHeight / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger inSampleSize).

			long totalPixels = originWidth * originHeight / inSampleSize;

			// Anything more than 2x the requested pixels we'll sample down further
			final long totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels > totalReqPixelsCap) {
				inSampleSize *= 2;
				totalPixels /= 2;
			}
		}
		return inSampleSize;
	}
}
