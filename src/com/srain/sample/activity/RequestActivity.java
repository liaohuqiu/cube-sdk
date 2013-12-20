package com.srain.sample.activity;

import com.srain.cube.Cube;
import com.srain.cube.request.CacheableRequestOnSuccHandler;
import com.srain.cube.request.JsonData;
import com.srain.cube.request.RequestOnSuccHandler;
import com.srain.sample.data.SampleData;
import com.srain.sdk.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

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
