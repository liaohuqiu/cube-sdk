package com.srain.cube.image;

public class ImageReuseInfo {

	private String mIndentitySize;
	private String[] mResuzeSzie;

	public ImageReuseInfo(String indentitySize, String[] reuseSize) {
		mIndentitySize = indentitySize;
		mResuzeSzie = reuseSize;
	}

	public String getIndentitySize() {
		return mIndentitySize;
	}

	public String[] getResuzeSize() {
		return mResuzeSzie;
	}
}
