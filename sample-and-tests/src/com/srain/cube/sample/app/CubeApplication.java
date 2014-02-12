package com.srain.cube.sample.app;

import android.app.Application;

import com.srain.cube.Cube;

public class CubeApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// other code
		// ..

		Cube.onCreate(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		// other code
		// ...

		Cube.onTerminate();
	}
}