package com.srain.cube.views.list;

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
public abstract class ListViewDataAdapterBase<ItemDataType> extends BaseAdapter {

	protected ViewHolderCreator<ItemDataType> mViewHolderCreator;

	/**
	 * 
	 * @param viewHolderCreator
	 *            The view holder creator will create a View Holder that extends {@link ViewHolderBase}
	 */
	public ListViewDataAdapterBase(ViewHolderCreator<ItemDataType> viewHolderCreator) {
		mViewHolderCreator = viewHolderCreator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemDataType itemData = getItem(position);
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			ViewHolderBase<ItemDataType> holderBase = mViewHolderCreator.createViewHodler();

			if (holderBase != null) {
				convertView = holderBase.createView(inflater);
				if (convertView != null) {
					holderBase.setItemData(position);
					holderBase.showData(position, itemData);
					convertView.setTag(holderBase);
				}
			}

		} else {
			ViewHolderBase<ItemDataType> holderBase = (ViewHolderBase<ItemDataType>) convertView.getTag();
			if (holderBase != null) {
				holderBase.setItemData(position);
				holderBase.showData(position, itemData);
			}
		}
		return convertView;
	}

	@Override
	public abstract ItemDataType getItem(int position);
}
