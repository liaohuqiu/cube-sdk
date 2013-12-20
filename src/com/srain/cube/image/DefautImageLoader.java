package com.srain.cube.image;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;

import com.srain.cube.app.lifecycle.LifeCycleComponent;
import com.srain.cube.image.ImageProvider.ImageCacheParams;

/**
 * A simple encapsulate of ImageLoader.
 * 
 * This loader will put a backgound to imageview when the image is loading.
 * 
 * @author huqiu.lhq
 */
public class DefautImageLoader extends ImageLoader implements LifeCycleComponent {

	private boolean mFadeInBitmap = false;
	protected ImageProviderAsync mImageProvider;

	public DefautImageLoader(Context context, ImageProviderAsync imageProvider, IWorker worker, IImageResizer imageResizer) {
		super(context, imageProvider, worker, imageResizer);
		mImageProvider = imageProvider;
	}

	/**
	 * create a DefautImageLoader which is related
	 */
	public static DefautImageLoader create(Context context, String cacheDir) {

		DefaultWorker worker = new DefaultWorker();
		ImageCacheParams cacheParams = new ImageCacheParams(context, cacheDir);
		cacheParams.setMemCacheSizePercent(0.25f);
		ImageProviderAsync imageProvider = new ImageProviderAsync(cacheParams, worker);
		imageProvider.initDiskCacheAsync();

		DefautImageLoader loader = new DefautImageLoader(context, imageProvider, worker, new DefaultResizer());
		return loader;
	}

	/**
	 * If set to true, the image will fade-in once it has been loaded by the background thread.
	 */
	public void setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap = fadeIn;
	}

	/**
	 * Load image.
	 */
	public void load(final RecyclingImageView imageView, String url, int requestWidth, int requestHeight, final int loadingRes) {
		final ImageTask imageTask = new ImageTask(url, requestWidth, requestWidth);
		imageTask.setRelatedImageView(imageView);
		imageTask.setLoadHandler(new ILoadHandler() {

			@Override
			public void onLoading(ImageTask imageTask) {
				ImageView imageView = imageTask.getWeakReferenceImageView();
				if (imageView != null) {
					imageView.setImageDrawable(null);
					imageView.setBackgroundResource(loadingRes);
				}
			}

			@Override
			public void onLoadFinish(ImageTask imageTask, BitmapDrawable drawable) {
				ImageView imageView = imageTask.getWeakReferenceImageView();
				if (imageView != null) {
					imageView.setBackgroundResource(0);
					imageView.setPadding(0, 0, 0, 0);

					if (mFadeInBitmap) {
						// Transition drawable with a transparent drawable and the final drawable
						final TransitionDrawable td = new TransitionDrawable(new Drawable[] { new ColorDrawable(android.R.color.transparent), drawable });

						imageView.setImageDrawable(td);
						td.startTransition(200);
					} else {
						imageView.setImageDrawable(drawable);
					}
				}
			}
		});
		processImageTask(imageTask);
	}

	/**
	 * Load the image in advance.
	 */
	public void preLoadImages(String[] urls) {
		int len = urls.length;
		len = 10;
		for (int i = 0; i < len; i++) {
			final ImageTask imageTask = new ImageTask(urls[i]);
			processImageTask(imageTask);
		}
	}

	@Override
	public void onResume() {
		resumeWork();
	}

	@Override
	public void onStop() {
		stopWork();
		mImageProvider.flushDishCache();
	}

	@Override
	public void onDestroy() {
		destory();
		mImageProvider.closeDiskCache();
	}
}
