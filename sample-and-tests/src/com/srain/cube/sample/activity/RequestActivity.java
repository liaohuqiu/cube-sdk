package com.srain.cube.sample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.srain.cube.Cube;
import com.srain.cube.request.CacheableRequestOnSuccHandler;
import com.srain.cube.request.JsonData;
import com.srain.cube.request.RequestOnSuccHandler;
import com.srain.cube.sample.R;
import com.srain.cube.sample.data.SampleData;

public class RequestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Cube.init(getApplication());
		testRequest();
	}

	private void testRequest() {
		String msg = "Hello.";

		SampleData.getRequestSampleData(msg, new RequestOnSuccHandler() {

			@Override
			public void onRequestSucc(JsonData jsonData) {
				Log.i("test", String.format("Request onRequestSucc data: %s", jsonData));
			}
		});
		SampleData.getCacheableRequestSampleData(msg, new CacheableRequestOnSuccHandler() {

			@Override
			public void onRequestSucc(JsonData jsonData) {
				Log.i("test", String.format("CacheableRequest onRequestSucc data: %s", jsonData));
			}

			@Override
			public void onCacheData(JsonData cacheData, boolean outoufDate) {
				Log.i("test", String.format("CacheableRequest onCacheData data: %s", cacheData));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
}
