package com.srain.cube.image;

public class ImageReuseInfo {

	private String mIdentitySize;
	private String[] mResuzeSzie;

	public ImageReuseInfo(String indentitySize, String[] reuseSize) {
		mIdentitySize = indentitySize;
		mResuzeSzie = reuseSize;
	}

	public String getIdentitySize() {
		return mIdentitySize;
	}

	public String[] getReuseSizeList() {
		return mResuzeSzie;
	}
}