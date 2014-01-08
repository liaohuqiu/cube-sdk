package com.srain.cube.sample.ui.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.srain.cube.sample.R;
import com.srain.cube.sample.activity.TitleBaseActivity;
import com.srain.cube.sample.data.Images;
import com.srain.cube.sample.ui.imagelist.ImageListViewManager;
import com.srain.cube.sample.ui.imagelist.ImageListViewManager.IImageListView;
import com.srain.cube.sample.ui.imagelist.ImageListViewManager.ListViewType;
import com.srain.cube.sample.ui.views.header.TitleHeaderBar;

public class ImageListFragment extends Fragment {

	private ImageListViewManager mImageListViewManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.image_list_fragment, container, false);
		mImageListViewManager = new ImageListViewManager();

		mImageListViewManager.addListView((IImageListView) v.findViewById(R.id.ly_image_list_small));
		mImageListViewManager.addListView((IImageListView) v.findViewById(R.id.ly_image_list_grid));
		mImageListViewManager.addListView((IImageListView) v.findViewById(R.id.ly_image_list_big));

		final TextView textView = (TextView) v.findViewById(R.id.tv_image_list_fragment_change);

		textView.setText("next: " + mImageListViewManager.getCurrentListViewType().next());
		TitleHeaderBar bar = ((TitleBaseActivity) (getActivity())).getTitleHeaderBar();
		bar.getRightTextView().setText("change layout");
		bar.getRigthViewContainer().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				ListViewType next = mImageListViewManager.getCurrentListViewType().next();
				mImageListViewManager.showListType(next);
				textView.setText("next: " + next.next());
			}
		});
		mImageListViewManager.showImageList(Images.imageUrls);
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// switch (item.getItemId()) {
		// case R.id.clear_cache:
		// // mImageLoader.clearCache();
		// Toast.makeText(getActivity(), R.string.clear_cache_complete_toast, Toast.LENGTH_SHORT).show();
		// return true;
		// }
		return super.onOptionsItemSelected(item);
	}
}
