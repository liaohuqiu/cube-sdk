/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.srain.sdk.image;

import java.io.FileDescriptor;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

/**
 * A simple subclass of {@link ImageWorker} that resizes images from resources given a target width and height. Useful for when the input images might be too large to simply load directly into memory.
 */
public class ImageResizer {

	/**
	 * Decode and sample down a bitmap from resources to the requested width and height.
	 * 
	 * @param res
	 *            The resources object containing the image data
	 * @param resId
	 *            The resource id of the image data
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param cache
	 *            The ImageProvider used to find candidate bitmaps for use with inBitmap
	 * @return A bitmap sampled down from the original with the same aspect ratio and dimensions that are equal to or greater than the requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, ImageRequest imageRequest, ImageProvider cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);

		// Calculate inSampleSize
		options.inSampleSize = imageRequest.getInSampleSize(options);

		// If we're running on Honeycomb or newer, try to use inBitmap
		tryAddInBitmapOptions(options, cache);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Decode and sample down a bitmap from a file to the requested width and height.
	 * 
	 * @param filename
	 *            The full path of the file to decode
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param cache
	 *            The ImageProvider used to find candidate bitmaps for use with inBitmap
	 * @return A bitmap sampled down from the original with the same aspect ratio and dimensions that are equal to or greater than the requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromFile(String filename, ImageRequest imageRequest, ImageProvider cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);

		// Calculate inSampleSize
		options.inSampleSize = imageRequest.getInSampleSize(options);

		// If we're running on Honeycomb or newer, try to use inBitmap
		tryAddInBitmapOptions(options, cache);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filename, options);
	}

	/**
	 * Decode and sample down a bitmap from a file input stream to the requested width and height.
	 * 
	 * @param fileDescriptor
	 *            The file descriptor to read from
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @param cache
	 *            The ImageProvider used to find candidate bitmaps for use with inBitmap
	 * @return A bitmap sampled down from the original with the same aspect ratio and dimensions that are equal to or greater than the requested width and height
	 */
	public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, ImageRequest imageRequest, ImageProvider cache) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		// Calculate inSampleSize
		options.inSampleSize = imageRequest.getInSampleSize(options);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		// If we're running on Honeycomb or newer, try to use inBitmap
		tryAddInBitmapOptions(options, cache);

		return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
	}

	/**
	 * If we're running on Honeycomb or newer, try to use inBitmap
	 * 
	 * @param options
	 * @param cache
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void tryAddInBitmapOptions(BitmapFactory.Options options, ImageProvider cache) {
		// inBitmap only works with mutable bitmaps so force the decoder to
		// return mutable bitmaps.
		options.inMutable = true;

		if (cache != null) {
			// Try and find a bitmap to use for inBitmap
			Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

			if (inBitmap != null) {
				options.inBitmap = inBitmap;
			}
		}
	}
}
