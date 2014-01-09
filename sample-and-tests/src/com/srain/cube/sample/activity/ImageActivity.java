package com.srain.cube.sample.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.srain.cube.sample.R;
import com.srain.cube.sample.ui.fragment.imagelist.BigListViewFragment;
import com.srain.cube.sample.ui.fragment.imagelist.GridListViewFragment;
import com.srain.cube.sample.ui.fragment.imagelist.SmallListViewFragment;

public class ImageActivity extends TitleBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_list);
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		String type = getIntent().getExtras().getString("type");

		if (type.equals("grid")) {
			ft.add(R.id.fragment_container, new GridListViewFragment(), GridListViewFragment.class.toString());
		} else if (type.equals("big")) {
			ft.add(R.id.fragment_container, new BigListViewFragment(), BigListViewFragment.class.toString());
		} else {
			ft.add(R.id.fragment_container, new SmallListViewFragment(), SmallListViewFragment.class.toString());
		}
		ft.commit();
		setHeaderTitle("Image List Demo");
	}
}