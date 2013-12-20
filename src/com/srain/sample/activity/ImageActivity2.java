package com.srain.sample.activity;

import com.srain.cube.app.XActivity;
import com.srain.sample.ui.fragment.ImageListFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class ImageActivity2 extends XActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(android.R.id.content, new ImageListFragment(), "test");
		ft.commit();
	}
}