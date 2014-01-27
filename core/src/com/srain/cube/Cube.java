package com.srain.cube;

import com.srain.cube.util.LocalDisplay;
import com.srain.cube.util.NetworkStatusManager;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class Cube {

	private static Cube instance;

	private Application mApplication;

	public static void init(Application app) {
		instance = new Cube(app);
	}

	private Cube(Application application) {
		mApplication = application;

		// local display
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(dm);
		LocalDisplay.init(dm);

		// network status
		NetworkStatusManager.init(application);
	}

	public static Cube getInstance() {
		return instance;
	}

	public Context getContext() {
		return mApplication;
	}

	public String getRootDirNameInSDCard() {
		return "cube_sdk";
	}

}
