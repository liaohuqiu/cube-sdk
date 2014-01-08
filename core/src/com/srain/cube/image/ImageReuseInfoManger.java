package com.srain.cube.image;

public class ImageReuseInfoManger {

	private int[] mSize;

	public ImageReuseInfoManger(int[] size) {
		mSize = size;
	}

	public ImageReuseInfo create(int thisSize) {
		return new ImageReuseInfo(thisSize, mSize);
	}
}