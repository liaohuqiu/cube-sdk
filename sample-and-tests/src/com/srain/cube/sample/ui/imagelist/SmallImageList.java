package com.srain.cube.sample.ui.imagelist;

import java.util.Arrays;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.srain.cube.image.CubeImageView;
import com.srain.cube.sample.R;
import com.srain.cube.sample.image.SampleImageLoader;
import com.srain.cube.sample.ui.imagelist.ImageListViewManager.IImageListView;
import com.srain.cube.sample.ui.imagelist.ImageListViewManager.ListViewType;
import com.srain.cube.views.list.ListViewDataAdpter;
import com.srain.cube.views.list.ViewHolderBase;
import com.srain.cube.views.list.ViewHolderCreator;

public class SmallImageList extends ListView implements IImageListView {

	private SampleImageLoader mImageLoader;

	public SmallImageList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mImageLoader = SampleImageLoader.create(context);
	}

	@Override
	public ListViewType getListViewType() {
		return ListViewType.list_small;
	}

	@Override
	public void showImageList(String[] imageList) {

		ListViewDataAdpter<String> adpter = new ListViewDataAdpter<String>(new ViewHolderCreator<String>() {

			@Override
			public ViewHolderBase<String> createViewHodler() {
				return new ViewHodler();
			}
		});
		setAdapter(adpter);
		adpter.getDataList().addAll(Arrays.asList(imageList));
		adpter.notifyDataSetChanged();
	}

	private class ViewHodler extends ViewHolderBase<String> {

		private CubeImageView mImageView;

		@Override
		public View createView(LayoutInflater inflater) {
			View v = inflater.inflate(R.layout.item_image_list_small, null);
			mImageView = (CubeImageView) v.findViewById(R.id.tv_item_image_list_small);
			return v;
		}

		@Override
		public void showData(String itemData) {
			mImageView.loadImage(mImageLoader, itemData);
		}
	}
}