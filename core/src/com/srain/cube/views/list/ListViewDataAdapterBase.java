package com.srain.cube.views.list;

import java.util.HashSet;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.srain.cube.util.CLog;

/**
 * A adapter using View Holder to display the item of a list view;
 * 
 * @author huqiu.lhq
 * 
 * @param <ItemDataType>
 */
public abstract class ListViewDataAdapterBase<ItemDataType> extends BaseAdapter {

	private static String LOG_TAG = "cube_list";

	protected ViewHolderCreator<ItemDataType> mViewHolderCreator;
	protected HashSet<Integer> mCreatedTag = new HashSet<Integer>();
	private boolean mEnableCreateViewForMeasure = true;

	/**
	 * 
	 * @param viewHolderCreator
	 *            The view holder creator will create a View Holder that extends {@link ViewHolderBase}
	 */
	public ListViewDataAdapterBase(ViewHolderCreator<ItemDataType> viewHolderCreator) {
		mViewHolderCreator = viewHolderCreator;
	}

	public void setEnableCreateViewForMeasure(boolean enable) {
		mEnableCreateViewForMeasure = enable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mEnableCreateViewForMeasure && convertView == null) {

		}
		if (CLog.DEBUG_LIST) {
			Log.d(LOG_TAG, String.format("getView %s", position));
		}
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
