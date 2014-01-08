package com.srain.cube.util;

import android.util.DisplayMetrics;

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

	public static int getScaledWidthDPByDP(int desingDP) {
		double v = desingDP / 320f * SCREEN_WIDTH_DP;
		return (int) v;
	}

	public static double getScaledWidthDPByDP(double desingDP) {
		double v = desingDP / 320f * SCREEN_WIDTH_DP;
		return v;
	}

	public static int getScaledWidthPixelsByDP(int desingDP) {
		double v = desingDP / 320f * SCREEN_WIDTH_PIXELS;
		return (int) v;
	}

	public static double getScaledWidthPixelsByDP(double desingDP) {
		double v = desingDP / 320f * SCREEN_WIDTH_PIXELS;
		return v;
	}

	public static int getScaledWidthPixelsByDesignDP(int designDP) {
		double v = getScaledWidthPixelsByDP(getScaledWidthDPByDP(designDP));
		return (int) v;
	}

	public static double getScaledWidthPixelsByDesignDP(double designDP) {
		double v = getScaledWidthPixelsByDP(getScaledWidthDPByDP(designDP));
		return v;
	}
}
