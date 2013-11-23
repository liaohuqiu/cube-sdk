package com.srain.sdk;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.srain.sdk.file.FileUtil;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final String path = FileUtil.wantFile(FileUtil.wantFilesPath(this, true, "etao") + "/d1/d2/d3", "test1.json").getAbsolutePath();

		com.loopj.android.http.AsyncHttpClient client = new AsyncHttpClient();
		client.get("http://www.taobao.com/go/rgn/etaoh5/search_index_php.php", new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);

				FileUtil.write(path, content);

				String str = FileUtil.read(path);
				Log.i("test", content);
				Log.i("test", str);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
