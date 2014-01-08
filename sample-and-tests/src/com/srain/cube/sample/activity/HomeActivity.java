package com.srain.cube.sample.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.srain.cube.sample.R;
import com.srain.cube.util.LocalDisplay;

public class HomeActivity extends TitleBaseActivity {

	private RelativeLayout mViewContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		mViewContainer = (RelativeLayout) findViewById(R.id.ly_home_container);

		setHeaderTitle("Cube Demo");
		setupList();
	}

	public void setupList() {

		int size = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(25 + 5 + 5)) / 3;
		int len = 30;

		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				int index = (Integer) v.getTag();

				if (index == 0) {
					Intent intent = new Intent();
					intent.setClass(HomeActivity.this, ImageActivity.class);
					startActivity(intent);
				}
			}
		};

		int horizontalSpacing = LocalDisplay.dp2px(5);
		int verticalSpacing = LocalDisplay.dp2px(10.5f);

		for (int i = 0; i < len; i++) {

			RelativeLayout.LayoutParams lyp = new RelativeLayout.LayoutParams(size, size);
			int row = i / 3;
			int clo = i % 3;
			int left = 0;
			int top = 0;

			if (clo > 0) {
				left = (horizontalSpacing + size) * clo;
			}
			if (row > 0) {
				top = (verticalSpacing + size) * row;
			}
			lyp.setMargins(left, top, 0, 0);
			View view = getView(i, size);
			view.setOnClickListener(listener);
			view.setTag(i);
			mViewContainer.addView(view, lyp);
		}
	}

	protected View getView(int index, int size) {
		LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.item_home, null);
		view.setLayoutParams(new LinearLayout.LayoutParams(size, size));
		return view;
	}

	@Override
	protected boolean enableDefaultBack() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
}
