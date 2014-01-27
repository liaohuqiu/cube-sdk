package com.srain.cube.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class CubeBitmapDrawable extends BitmapDrawable {

	public CubeBitmapDrawable(Resources res, Bitmap bitmap) {
		super(res, bitmap);
	}

	@Override
	public int getIntrinsicHeight() {
		return -1;
	}

	@Override
	public int getIntrinsicWidth() {
		return -1;
	}

	public int getOriginIntrinsicWidth() {
		return super.getIntrinsicWidth();
	}

	public int getOriginIntrinsicHeight() {
		return super.getIntrinsicHeight();
	}
}
