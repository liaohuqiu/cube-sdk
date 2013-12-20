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

package com.srain.cube.image;

import com.srain.cube.image.ImageLoader.IImageResizer;

import android.graphics.BitmapFactory;

/**
 * A default implemetions of {@link IImageResizer}
 * 
 * @author huqiu.lhq
 * 
 */
public class DefaultResizer implements IImageResizer {

	@Override
	public int getInSampleSize(ImageTask imageTask) {
		int size = calculateInSampleSize(imageTask.getOriginWidth(), imageTask.getOriginWidth(), imageTask.getRequestWidth(), imageTask.getRequestWidth());
		return size;
	}

	/**
	 * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap having a width and height equal to or larger than the requested width and height.
	 * 
	 * @param options
	 *            An options object with out* params already populated (run through a decode* method with inJustDecodeBounds==true
	 * @param reqWidth
	 *            The requested width of the resulting bitmap
	 * @param reqHeight
	 *            The requested height of the resulting bitmap
	 * @return The value to be used for inSampleSize
	 */
	public static int calculateInSampleSize(int originWidth, int originHeight, int reqWidth, int reqHeight) {

		int inSampleSize = 0;

		if (reqHeight <= 0 || reqHeight <= 0 || true) {
			return 1;
		}

		if (originWidth > reqHeight || originHeight > reqWidth) {

			double f = Math.sqrt(2);

			final int halfHeight = (int) (originWidth / f);
			final int halfWidth = (int) (originHeight / f);

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize += 2;
			}

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger inSampleSize).

			long totalPixels = originWidth * originHeight / inSampleSize;

			// Anything more than 2x the requested pixels we'll sample down further
			final long totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels > totalReqPixelsCap) {
				inSampleSize += 2;
				totalPixels /= 2;
			}
		}
		if (inSampleSize == 0) {
			inSampleSize = 1;
		}
		return 1;
		// return inSampleSize;
	}

	@Override
	public String getResizedUrl(ImageTask imageTask) {
		return imageTask.getRemoteUrl();
	}
}