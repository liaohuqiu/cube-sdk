package com.srain.cube.sample.ui.fragment.imagelist;

import java.util.Arrays;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.srain.cube.image.CubeImageView;
import com.srain.cube.sample.R;
import com.srain.cube.sample.data.Images;
import com.srain.cube.sample.image.SampleImageLoader;
import com.srain.cube.util.LocalDisplay;
import com.srain.cube.views.list.ListViewDataAdpter;
import com.srain.cube.views.list.ViewHolderBase;
import com.srain.cube.views.list.ViewHolderCreator;

public class BigListViewFragment extends Fragment {

	private static final int sBigImageSize = LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(10 + 10);

	private SampleImageLoader mImageLoader;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mImageLoader = SampleImageLoader.create(getActivity());
		final View v = inflater.inflate(R.layout.fragment_image_list_big, container, false);

		ListView listView = (ListView) v.findViewById(R.id.ly_image_list_big);

		ListViewDataAdpter<String> adpter = new ListViewDataAdpter<String>(new ViewHolderCreator<String>() {
			@Override
			public ViewHolderBase<String> createViewHodler() {
				return new ViewHodler();
			}
		});
		listView.setAdapter(adpter);
		adpter.getDataList().addAll(Arrays.asList(Images.imageUrls));
		adpter.notifyDataSetChanged();
		return v;
	}

	private class ViewHodler extends ViewHolderBase<String> {

		private CubeImageView mImageView;

		@Override
		public View createView(LayoutInflater inflater) {
			View view = inflater.inflate(R.layout.item_image_list_big, null);
			mImageView = (CubeImageView) view.findViewById(R.id.tv_item_image_list_big);

			LinearLayout.LayoutParams lyp = new LinearLayout.LayoutParams(sBigImageSize, sBigImageSize);
			mImageView.setLayoutParams(lyp);

			return view;
		}

		@Override
		public void showData(String itemData) {
			mImageView.loadImage(mImageLoader, itemData);
		}
	}
}
