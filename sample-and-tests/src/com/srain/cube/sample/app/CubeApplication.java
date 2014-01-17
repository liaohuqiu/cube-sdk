package com.srain.cube.sample.app;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.srain.cube.file.FileUtil;
import com.srain.cube.image.imple.LruImageFileCache;
import com.srain.cube.util.LocalDisplay;
import com.srain.cube.util.SystemWather;

public class CubeApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		SystemWather.init(this);
		SystemWather.getInstance().run();

		LruImageFileCache.getDefault(this).initDiskCacheAsync();

		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		LocalDisplay.init(dm);
		
		FileUtil.test(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}