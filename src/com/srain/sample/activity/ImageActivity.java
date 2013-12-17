package com.srain.sample.activity;

import com.srain.sdk.app.XActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class ImageActivity extends XActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(android.R.id.content, new ImageGridFragment(), "test");
		ft.commit();
	}
}