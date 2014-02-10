package com.srain.cube.image.iface;

import android.graphics.BitmapFactory;

import com.srain.cube.image.ImageTask;

/**
 * A ImageResizer process the resize logical when loading image from network an disk.
 */
public interface ImageResizer {

	/**
	 * Return the {@link BitmapFactory.Options#inSampleSize}, which will be used when load the image from the disk.
	 * 
	 * You should better calculate this value accroding the hard device of the mobile.
	 */
	public int getInSampleSize(ImageTask imageTask);

	/**
	 * If you have a thrumb web service which can return multiple size image acrroding the url,
	 * 
	 * you can implements this method to return the specified url accoding the request size.
	 */
	public String getResizedUrl(ImageTask imageTask);
}