package com.srain.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.srain.cube.app.XActivity;
import com.srain.cube.app.lifecycle.LifeCycleComponentManager;
import com.srain.cube.image.DefautImageLoader;
import com.srain.sample.data.Images;
import com.srain.sdk.R;

public class MainActivity extends XActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DefautImageLoader imageLoader = DefautImageLoader.create(this, "thumbs");
		LifeCycleComponentManager.tryAddComponentToContainer(imageLoader, this);
		imageLoader.preLoadImages(Images.imageUrls);

		findViewById(R.id.btn_test).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ImageActivity.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.btn_test2).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, ImageActivity2.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
}
