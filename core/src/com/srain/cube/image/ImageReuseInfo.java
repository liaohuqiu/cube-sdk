package com.srain.cube.image;

public class ImageReuseInfo {

	private int mIndentitySize;
	private int[] mResuzeSzie;

	public ImageReuseInfo(int indentitySize, int[] reuseSize) {
		mIndentitySize = indentitySize;
		mResuzeSzie = reuseSize;
	}

	public int getIndentitySize() {
		return mIndentitySize;
	}

	public int[] getResuzeSize() {
		return mResuzeSzie;
	}
}
