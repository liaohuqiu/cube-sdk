package com.srain.cube.image;

public class ImageReuseInfoManger {

	private String[] mSize;

	public ImageReuseInfoManger(String[] size) {
		mSize = size;
	}

	public ImageReuseInfo create(String thisSize) {
		return new ImageReuseInfo(thisSize, mSize);
	}
}