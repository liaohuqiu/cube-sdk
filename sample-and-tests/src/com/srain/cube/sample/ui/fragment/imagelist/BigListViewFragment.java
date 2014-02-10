package com.srain.cube.sample.ui.fragment.imagelist;

import java.util.Arrays;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.srain.cube.image.CubeImageView;
import com.srain.cube.sample.R;
import com.srain.cube.sample.activity.TitleBaseFragment;
import com.srain.cube.sample.data.Images;
import com.srain.cube.sample.image.SampleImageLoader;
import com.srain.cube.util.LocalDisplay;
import com.srain.cube.views.list.ListViewDataAdapter;
import com.srain.cube.views.list.ViewHolderBase;
import com.srain.cube.views.list.ViewHolderCreator;

public class BigListViewFragment extends TitleBaseFragment {

	private static final int sBigImageSize = LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(10 + 10);

	private SampleImageLoader mImageLoader;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mImageLoader = SampleImageLoader.create(getActivity());
		final View v = inflater.inflate(R.layout.fragment_image_list_big, container, false);

		ListView listView = (ListView) v.findViewById(R.id.ly_image_list_big);

		ListViewDataAdapter<String> adpter = new ListViewDataAdapter<String>(new ViewHolderCreator<String>() {
			@Override
			public ViewHolderBase<String> createViewHodler() {
				return new ViewHodler();
			}
		});
		listView.setAdapter(adpter);
		adpter.getDataList().addAll(Arrays.asList(Images.imageUrls));
		adpter.notifyDataSetChanged();

		setHeaderTitle("Big Image");
		return v;
	}

	private class ViewHodler extends ViewHolderBase<String> {

		private CubeImageView mImageView;

		@Override
		public View createView(LayoutInflater inflater) {
			View view = inflater.inflate(R.layout.item_image_list_big, null);
			mImageView = (CubeImageView) view.findViewById(R.id.tv_item_image_list_big);
			mImageView.setScaleType(ScaleType.CENTER_CROP);

			LinearLayout.LayoutParams lyp = new LinearLayout.LayoutParams(sBigImageSize, sBigImageSize);
			mImageView.setLayoutParams(lyp);

			return view;
		}

		@Override
		public void showData(int position, String itemData) {
			mImageView.loadImage(mImageLoader, itemData);
		}
	}
}
