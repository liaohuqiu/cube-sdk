package com.srain.sample.activity;

import com.srain.sample.data.SampleData;
import com.srain.sdk.Cube;
import com.srain.sdk.R;
import com.srain.sdk.request.CacheableRequestOnSuccHandler;
import com.srain.sdk.request.JsonData;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Cube.init(getApplication());
		testFileUtil();
	}

	private void testFileUtil() {
		SampleData.getSampleData(new CacheableRequestOnSuccHandler() {

			@Override
			public void onRequestSucc(JsonData jsonData) {
				Log.i("test", String.format("onRequestSucc data: %s", jsonData));
			}

			@Override
			public void onCachedPreviousData(JsonData previousJsonData) {
				Log.i("test", String.format("prev data: %s", previousJsonData));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
