package com.srain.sdk.image;

import android.graphics.drawable.BitmapDrawable;

public class ImageResult {

	private BitmapDrawable mBitmapDrawable;

	public ImageResult(BitmapDrawable bitmapDrawable) {
		mBitmapDrawable = bitmapDrawable;
	}

	public BitmapDrawable getBitmapDrawable() {
		return mBitmapDrawable;
	}
}