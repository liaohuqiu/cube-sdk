package com.srain.sdk;

import com.loopj.android.http.AsyncHttpClient;

import android.app.Application;
import android.content.Context;

public class Cube {

	private AsyncHttpClient mAsyncHttpClient;

	private static Cube instance;

	private Application mApplication;

	public static void init(Application app) {
		instance = new Cube(app);
		instance.mAsyncHttpClient = new AsyncHttpClient();
	}

	private Cube(Application application) {
		mApplication = application;
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

	public AsyncHttpClient getAsyncHttpClient() {
		return mAsyncHttpClient;
	}
}
