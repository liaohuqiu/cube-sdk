package com.srain.cube.sample.app;

import android.app.Application;

import com.srain.cube.Cube;
import com.srain.cube.image.imple.LruImageFileCache;
import com.srain.cube.util.CLog;

public class CubeApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		LruImageFileCache.getDefault(this).initDiskCacheAsync();

		CLog.DEBUG_IMAGE = false;
		CLog.DEBUG_LIST = true;

		Cube.init(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}