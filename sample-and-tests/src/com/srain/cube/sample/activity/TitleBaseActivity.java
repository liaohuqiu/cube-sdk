package com.srain.cube.sample.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.srain.cube.app.XActivity;
import com.srain.cube.sample.R;
import com.srain.cube.sample.ui.views.HeaderBar;

public abstract class TitleBaseActivity extends XActivity {

	protected HeaderBar mHeaderBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getLayouId());
		mHeaderBar = (HeaderBar) findViewById(R.id.ly_header_bar);

		if (enableDefaultBack()) {
			mHeaderBar.setLeftText("back");
			mHeaderBar.setLeftClickHandler(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}
	}

	protected boolean enableDefaultBack() {
		return true;
	}

	protected abstract int getLayouId();
}
