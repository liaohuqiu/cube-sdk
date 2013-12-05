package com.srain.sample.activity;

import com.srain.sample.data.SampleData;
import com.srain.sdk.Cube;
import com.srain.sdk.R;
import com.srain.sdk.request.CacheableRequestOnSuccHandler;
import com.srain.sdk.request.JsonData;
import com.srain.sdk.request.RequestOnSuccHandler;

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
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
