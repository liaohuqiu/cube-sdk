package com.srain.cube.util;

import android.util.DisplayMetrics;
import android.view.View;

public class LocalDisplay {

	public static int SCREEN_WIDTH_PIXELS;
	public static int SCREEN_HEIGHT_PIXELS;
	public static float SCREEN_DENSITY;
	public static int SCREEN_WIDTH_DP;
	public static int SCREEN_HEIGHT_DP;

	public static void init(DisplayMetrics dm) {

		SCREEN_WIDTH_PIXELS = dm.widthPixels;
		SCREEN_HEIGHT_PIXELS = dm.heightPixels;
		SCREEN_DENSITY = dm.density;
		SCREEN_WIDTH_DP = (int) (SCREEN_WIDTH_PIXELS / dm.density);
		SCREEN_HEIGHT_DP = (int) (SCREEN_HEIGHT_PIXELS / dm.density);
	}

	public static int dp2px(float dp) {
		final float scale = SCREEN_DENSITY;
		return (int) (dp * scale + 0.5f);
	}

	public static int designedDP2px(float desingDP) {
		if (SCREEN_WIDTH_DP != 320) {
			desingDP = desingDP * SCREEN_WIDTH_DP / 320f;
		}
		return dp2px(desingDP);
	}

	public static void setPadding(final View view, float left, float top, float right, float bottom) {
		view.setPadding(designedDP2px(left), dp2px(top), designedDP2px(right), dp2px(bottom));
	}
}
