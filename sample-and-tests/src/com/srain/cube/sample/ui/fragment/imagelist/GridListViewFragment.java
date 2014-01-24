package com.srain.cube.sample.ui.fragment.imagelist;

import java.util.Arrays;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.srain.cube.image.CubeImageView;
import com.srain.cube.sample.R;
import com.srain.cube.sample.data.Images;
import com.srain.cube.sample.image.SampleImageLoader;
import com.srain.cube.util.LocalDisplay;
import com.srain.cube.views.list.ListViewDataAdapter;
import com.srain.cube.views.list.ViewHolderBase;
import com.srain.cube.views.list.ViewHolderCreator;

public class GridListViewFragment extends Fragment {

	private static final int sGirdImageSize = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(10 + 10 + 10)) / 2;
	private SampleImageLoader mImageLoader;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mImageLoader = SampleImageLoader.create(getActivity());

		final View v = inflater.inflate(R.layout.fragment_image_gird, container, false);
		GridView gridListView = (GridView) v.findViewById(R.id.ly_image_list_grid);

		ListViewDataAdapter<String> adpter = new ListViewDataAdapter<String>(new ViewHolderCreator<String>() {
			@Override
			public ViewHolderBase<String> createViewHodler() {
				return new ViewHolder();
			}
		});
		gridListView.setAdapter(adpter);
		adpter.getDataList().addAll(Arrays.asList(Images.imageUrls));
		adpter.notifyDataSetChanged();
		return v;
	}

	private class ViewHolder extends ViewHolderBase<String> {

		private CubeImageView mImageView;

		@Override
		public View createView(LayoutInflater inflater) {
			View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_image_list_grid, null);
			mImageView = (CubeImageView) view.findViewById(R.id.iv_item_iamge_list_grid);

			LinearLayout.LayoutParams lyp = new LinearLayout.LayoutParams(sGirdImageSize, sGirdImageSize);
			mImageView.setLayoutParams(lyp);
			return view;
		}

		@Override
		public void showData(int position, String itemData) {
			mImageView.loadImage(mImageLoader, itemData);
		}
	}
}
