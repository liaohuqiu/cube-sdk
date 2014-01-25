package com.srain.cube.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;

public class CubeBitmapDrawable extends BitmapDrawable {

	private int mTargetDensity;

	public CubeBitmapDrawable(Resources res, Bitmap bitmap) {
		super(res, bitmap);
	}

	@Override
	public void setTargetDensity(int density) {
		super.setTargetDensity(density);
		if (mTargetDensity != density) {
			mTargetDensity = density == 0 ? DisplayMetrics.DENSITY_DEFAULT : density;
		}
	}

	public int getTargetDensity() {
		if (mTargetDensity == 0) {
			mTargetDensity = DisplayMetrics.DENSITY_DEFAULT;
		}
		return mTargetDensity;
	}

	@Override
	public int getIntrinsicHeight() {
		return -1;
	}

	@Override
	public int getIntrinsicWidth() {
		return -1;
	}
}
