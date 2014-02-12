package com.srain.cube.sample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.srain.cube.sample.R;

public class RequestActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		testRequest();
	}

	private void testRequest() {
		// String msg = "Hello.";
		// SampleData.getRequestSampleData(msg, new RequestFinishHandler<JsonData>() {
		//
		// @Override
		// public void onRequestFinish(JsonData data) {
		// Log.i("test", String.format("Request onRequestSucc data: %s", jsonData));
		// }
		// });
		// SampleData.getCacheableRequestSampleData(msg, new CacheableRequestOnSuccHandler() {
		//
		// @Override
		// public void onRequestSucc(JsonData jsonData) {
		// Log.i("test", String.format("CacheableRequest onRequestSucc data: %s", jsonData));
		// }
		//
		// @Override
		// public void onCacheData(JsonData cacheData, boolean outoufDate) {
		// Log.i("test", String.format("CacheableRequest onCacheData data: %s", cacheData));
		// }
		// });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
}
