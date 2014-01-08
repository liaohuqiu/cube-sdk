package com.srain.cube.sample.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.srain.cube.sample.R;
import com.srain.cube.sample.ui.fragment.ImageListFragment;

public class ImageActivity extends TitleBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_list);
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.fragment_container, new ImageListFragment(), ImageListFragment.class.toString());
		ft.commit();

		setHeaderTitle("Image List Demo");
	}
}