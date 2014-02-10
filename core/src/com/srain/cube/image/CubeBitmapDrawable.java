package com.srain.cube.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

/**
 * Scale or convert the Bitmap according the ScaclType, make the Bitmap fit the ImageView.
 * 
 * Then return Intrinsic size as -1 to make ImageView will not requestLayout() after setImageDrawable().
 * 
 * In the method `updateDrawalbe()` in ImageView, mDrawMatrix will be set to null due to the
 * 
 * @author srain
 */
public class CubeBitmapDrawable extends BitmapDrawable {

	public CubeBitmapDrawable(Resources res, Bitmap bitmap) {
		super(res, bitmap);
	}

	// @Override
	// public int getIntrinsicHeight() {
	// return -1;
	// }
	//
	// @Override
	// public int getIntrinsicWidth() {
	// return -1;
	// }
}
