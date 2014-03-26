package com.srain.cube.sample.ui.fragment.imagelist;

import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import com.srain.cube.image.CubeImageView;
import com.srain.cube.image.ImageLoader;
import com.srain.cube.image.ImageLoaderFactory;
import com.srain.cube.image.ImageReuseInfo;
import com.srain.cube.image.imple.DefaultImageLoadHandler;
import com.srain.cube.sample.R;
import com.srain.cube.sample.activity.TitleBaseFragment;
import com.srain.cube.sample.data.Images;
import com.srain.cube.util.LocalDisplay;
import com.srain.cube.views.list.ListViewDataAdapter;
import com.srain.cube.views.list.ViewHolderBase;
import com.srain.cube.views.list.ViewHolderCreator;

public class SmallListViewFragment extends TitleBaseFragment {

	private ImageLoader mImageLoader;
	public static final int sSmallImageSize = LocalDisplay.dp2px(100);

	private static final ImageReuseInfo sSmallImageReuseInfo = Images.sImageReuseInfoManger.create("small_180");

	@Override
	public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mImageLoader = ImageLoaderFactory.create(getActivity());
		((DefaultImageLoadHandler) mImageLoader.getImageLoadHandler()).setImageRounded(true, 25);

		final View v = inflater.inflate(R.layout.fragment_image_list_small, container, false);

		ListView gridListView = (ListView) v.findViewById(R.id.ly_image_list_small);

		ListViewDataAdapter<String> adpter = new ListViewDataAdapter<String>(new ViewHolderCreator<String>() {
			@Override
			public ViewHolderBase<String> createViewHodler() {
				return new ViewHodler();
			}
		});
		gridListView.setAdapter(adpter);
		adpter.getDataList().addAll(Arrays.asList(Images.imageUrls));
		adpter.notifyDataSetChanged();

		setHeaderTitle("Small List");
		return v;
	}

	private class ViewHodler extends ViewHolderBase<String> {

		private CubeImageView mImageView;

		@Override
		public View createView(LayoutInflater inflater) {
			View v = inflater.inflate(R.layout.item_image_list_small, null);
			mImageView = (CubeImageView) v.findViewById(R.id.tv_item_image_list_small);
			mImageView.setScaleType(ScaleType.CENTER_CROP);
			return v;
		}

		@Override
		public void showData(int position, String itemData) {
			mImageView.loadImage(mImageLoader, itemData, sSmallImageReuseInfo);
		}
	}
}
