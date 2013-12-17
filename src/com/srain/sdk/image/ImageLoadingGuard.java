package com.srain.sdk.image;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class ImageLoadingGuard {

	private Resources mResources;
	private Bitmap mLoadingBitmap;

	public static ImageLoadingGuard create(Resources resources, Bitmap loadingBitmap) {
		ImageLoadingGuard imageLoadingGuard = new ImageLoadingGuard();

		imageLoadingGuard.mResources = resources;
		imageLoadingGuard.mLoadingBitmap = loadingBitmap;
		return imageLoadingGuard;
	}

	public <T> void onLoading(ImageView imageView, T badge) {
		final BadgeDrawable<T> asyncDrawable = new BadgeDrawable<T>(mResources, mLoadingBitmap, badge);
		imageView.setImageDrawable(asyncDrawable);
	}

	public boolean cancelPotentialWork(ImageView imageView) {
		return true;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active work task (if any) associated with this imageView. null if there is no such task.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getImageViewBadage(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof BadgeDrawable<?>) {
				final BadgeDrawable<T> asyncDrawable = (BadgeDrawable<T>) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	/**
	 * A custom Drawable that will be attached to the imageView while the work is in progress. Contains a reference to the actual worker task, so that it can be stopped if a new binding is required, and makes sure that only the last started worker process can bind its result, independently of the finish order.
	 */
	private static class BadgeDrawable<T> extends BitmapDrawable {
		private final WeakReference<T> bitmapWorkerTaskReference;

		public BadgeDrawable(Resources res, Bitmap bitmap, T work) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<T>(work);
		}

		public T getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}
}
