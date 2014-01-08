package com.srain.cube.views.list;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * A adapter using View Holder to display the item of a list view;
 * 
 * @author huqiu.lhq
 * 
 * @param <ItemDataType>
 */
public class ListViewDataAdpter<ItemDataType> extends BaseAdapter {

	protected ViewHolderCreator<ItemDataType> mViewHolderCreator;
	protected ArrayList<ItemDataType> mItemDataList = new ArrayList<ItemDataType>();

	/**
	 * 
	 * @param viewHolderCreator
	 *            The view holder creator will create a View Holder that extends {@link ViewHolderBase}
	 */
	public ListViewDataAdpter(ViewHolderCreator<ItemDataType> viewHolderCreator) {
		mViewHolderCreator = viewHolderCreator;
	}

	public ArrayList<ItemDataType> getDataList() {
		return mItemDataList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ItemDataType itemData = mItemDataList.get(position);
		if (convertView == null) {

			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			ViewHolderBase<ItemDataType> holderBase = mViewHolderCreator.createViewHodler();

			if (holderBase != null) {
				convertView = holderBase.createView(inflater);
				holderBase.setItemData(itemData);
				holderBase.showData(itemData);
				convertView.setTag(holderBase);
			}

		} else {
			ViewHolderBase<ItemDataType> holderBase = (ViewHolderBase<ItemDataType>) convertView.getTag();
			if (holderBase != null) {
				holderBase.setItemData(itemData);
				holderBase.showData(itemData);
			}
		}
		return convertView;
	}

	@Override
	public int getCount() {
		return mItemDataList.size();
	}

	@Override
	public Object getItem(int position) {
		if (mItemDataList.size() <= position || position < 0) {
			return null;
		}
		return mItemDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
