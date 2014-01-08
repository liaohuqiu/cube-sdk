package com.srain.cube.sample.ui.imagelist;

import java.util.ArrayList;
import java.util.List;

import com.srain.cube.util.LocalDisplay;

import android.view.View;

public class ImageListViewManager {

	private List<IImageListView> mViewList = new ArrayList<IImageListView>();

	private ListViewType mCurrentListViewType = ListViewType.grid;

	public static final int sGirdImageSize = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(10 + 10 + 2)) / 2;
	public static final int sBigImageSize = LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(10 + 10);
	public static final int sSmallImageSize = LocalDisplay.dp2px(50);

	public interface IImageListView {
		public ListViewType getListViewType();

		public void showImageList(String[] imageList);
	}

	public enum ListViewType {
		list_small, grid, list_big;

		public String toString() {
			switch (this) {
			case list_small:
				return "small-list";
			case list_big:
				return "big-list";
			default:
				return "grid";
			}
		};

		public ListViewType next() {
			int index = (this.ordinal() + 1) % values().length;
			return values()[index];
		}
	}

	public void addListView(IImageListView view) {
		if (!mViewList.contains(view)) {
			mViewList.add(view);
		}
	}

	public void showListType(ListViewType listViewType) {
		if (mCurrentListViewType == listViewType) {
			return;
		}
		mCurrentListViewType = listViewType;
		for (IImageListView view : mViewList) {
			if (view.getListViewType() == listViewType) {
				((View) view).setVisibility(View.VISIBLE);
			} else {
				((View) view).setVisibility(View.GONE);
			}
		}
	}

	public void showImageList(String[] imageList) {
		for (IImageListView view : mViewList) {
			view.showImageList(imageList);
		}
	}

	public ListViewType getCurrentListViewType() {
		return mCurrentListViewType;
	}
}