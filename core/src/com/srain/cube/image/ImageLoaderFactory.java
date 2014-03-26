package com.srain.cube.image;

import android.content.Context;

import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.image.iface.ImageResizer;
import com.srain.cube.image.iface.ImageTaskExcutor;
import com.srain.cube.image.imple.DefaultImageLoadHandler;
import com.srain.cube.image.imple.DefaultImageTaskExecutor;
import com.srain.cube.image.imple.DefaultResizer;

/**
 * Manager the ImageTask loading list,
 * 
 * @author http://www.liaohuqiu.net
 */
public class ImageLoaderFactory {

	private static ImageResizer sDefaultImageResizer;
	private static ImageTaskExcutor sDefualtImageTaskExcutor;
	private static ImageLoadHandler sDefaultImageLoadHandler;
	private static ImageProvider sDefaultImageProvider;

	public static ImageLoader create(Context context) {
		ImageLoader imageLoader = create(context, sDefaultImageProvider, sDefualtImageTaskExcutor, sDefaultImageResizer, sDefaultImageLoadHandler);
		return imageLoader;
	}

	public static ImageLoader create(Context context, ImageProvider imageProvider, ImageTaskExcutor imageTaskExcutor, ImageResizer imageResizer, ImageLoadHandler imageLoadHandler) {

		if (imageProvider == null) {
			imageProvider = ImageProvider.getDefault(context);
		}

		if (imageTaskExcutor == null) {
			imageTaskExcutor = DefaultImageTaskExecutor.getInstance();
		}

		if (imageResizer == null) {
			imageResizer = DefaultResizer.getInstance();
		}

		if (imageLoadHandler == null) {
			imageLoadHandler = new DefaultImageLoadHandler(context);
		}
		return new ImageLoader(context, imageProvider, imageTaskExcutor, imageResizer, imageLoadHandler);
	}

	public static void setDefaultImageResizer(ImageResizer imageResizer) {
		sDefaultImageResizer = imageResizer;
	}

	public static void setDefaultImageTaskExcutor(ImageTaskExcutor imageTaskExcutor) {
		sDefualtImageTaskExcutor = imageTaskExcutor;
	}

	public static void setDefaultImageLoadHandler(ImageLoadHandler imageLoadHandler) {
		sDefaultImageLoadHandler = imageLoadHandler;
	}

	public static void setDefaultImageProvider(ImageProvider imageProvider) {
		sDefaultImageProvider = imageProvider;
	}
}
