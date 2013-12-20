package com.srain.cube.image;

import java.lang.ref.WeakReference;

import android.widget.ImageView;

/**
 * A imageView can be reused in a list and used to load diffrent image.
 * 
 * @author huqiu.lhq
 * 
 */
public class ImageViewBadge {

	private static final int IMAGEVIEW_TAG_KEY = 3 << 24;

	public static <T> void setBadage(ImageView imageView, T badge) {
		if (imageView == null) {
			return;
		}
		imageView.setTag(IMAGEVIEW_TAG_KEY, new WeakReference<T>(badge));
	}

	public static <T> T getBadge(ImageView imageView) {
		if (imageView == null) {
			return null;
		}
		if (imageView != null) {
			final Object object = imageView.getTag(IMAGEVIEW_TAG_KEY);
			if (object != null && object instanceof WeakReference<?>) {
				@SuppressWarnings("unchecked")
				WeakReference<T> badgeWrapper = (WeakReference<T>) object;
				return badgeWrapper.get();
			}
		}
		return null;
	}
}
