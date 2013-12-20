package com.srain.cube.image;

import android.content.Context;

import com.srain.cube.image.ImageProvider.ImageCacheParams;

/**
 * A simple encapsulate of ImageLoader.
 * 
 * This loader will put a backgound to imageview when the image is loading.
 * 
 * @author huqiu.lhq
 */
public class DefautGlobalImageLoader extends DefautImageLoader {

	private static ImageProviderAsync sImageProvider;
	private static final String GLOBLE_CACHE_DIR = "global_image_cache";
	private static DefaultWorker mDefaultWorker = new DefaultWorker();

	public DefautGlobalImageLoader(Context context, ImageProviderAsync imageProvider, IWorker worker, IImageResizer imageResizer) {
		super(context, imageProvider, worker, imageResizer);
	}

	public static DefautGlobalImageLoader getInstance(Context context) {
		DefautGlobalImageLoader loader = new DefautGlobalImageLoader(context, getGloabalImageProvider(context), mDefaultWorker, new DefaultResizer());
		return loader;
	}

	public static ImageProviderAsync getGloabalImageProvider(Context context) {
		if (sImageProvider == null) {
			ImageCacheParams cacheParams = new ImageCacheParams(context, GLOBLE_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.25f);
			sImageProvider = new ImageProviderAsync(cacheParams, mDefaultWorker);
			sImageProvider.initDiskCacheAsync();
		}
		return sImageProvider;
	}

	@Override
	public void onResume() {
		resumeWork();
	}

	@Override
	public void onStop() {
		stopWork();
	}

	@Override
	public void onDestroy() {
		destory();
	}
}
