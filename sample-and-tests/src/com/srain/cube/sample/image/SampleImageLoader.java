package com.srain.cube.sample.image;

import android.content.Context;

import com.srain.cube.app.lifecycle.IComponentContainer;
import com.srain.cube.app.lifecycle.LifeCycleComponent;
import com.srain.cube.app.lifecycle.LifeCycleComponentManager;
import com.srain.cube.image.ImageLoader;
import com.srain.cube.image.ImageProvider;
import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.image.iface.ImageResizer;
import com.srain.cube.image.iface.ImageTaskExcutor;
import com.srain.cube.image.imple.DefaultImageLoadHandler;
import com.srain.cube.image.imple.DefaultImageTaskExecutor;
import com.srain.cube.image.imple.DefaultResizer;
import com.srain.cube.sample.R;

public class SampleImageLoader extends ImageLoader implements LifeCycleComponent {

	public SampleImageLoader(Context context, ImageProvider imageProvider, ImageTaskExcutor executor, ImageResizer imageResizer, ImageLoadHandler imageLoadHandler) {
		super(context, imageProvider, executor, imageResizer, imageLoadHandler);

		if (!(context instanceof IComponentContainer)) {
			throw new IllegalArgumentException("context should impletemnts IComponentContainer");
		} else {
			LifeCycleComponentManager.tryAddComponentToContainer(this, context);
		}
	}

	public static SampleImageLoader create(Context context) {
		DefaultImageLoadHandler imageLoadHandler = new DefaultImageLoadHandler(context);
		imageLoadHandler.setLoadingBitmap(R.drawable.base_img_placeholder);
		SampleImageLoader imageLoader = new SampleImageLoader(context, ImageProvider.getDefault(context), DefaultImageTaskExecutor.getInstance(), DefaultResizer.getInstance(), imageLoadHandler);
		return imageLoader;
	}

	@Override
	public void onRestart() {
		recoverWork();
	}

	@Override
	public void onPause() {
		pauseWork();
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
