package com.srain.cube.views.list;

import android.view.LayoutInflater;
import android.view.View;

/**
 * As described in
 * 
 * <a href="http://developer.android.com/training/improving-layouts/smooth-scrolling.html">http://developer.android.com/training/improving-layouts/smooth-scrolling.html</a>
 * 
 * Using A View Holder in Listview getView() method is a good practice in using listview;
 * 
 * This class encapsulate the base operate of a View Holder: createView / showData
 * 
 * @author huqiu.lhq
 * 
 * @param <ItemDataType>
 *            the generic type of the data in each item
 */
public abstract class ViewHolderBase<ItemDataType> {

	protected ItemDataType mItemData;
	protected ItemDataType mLastItemData;
	
	/**
	 * create a view from resource xml file, and hold the view that may be used in displaying data.
	 */
	public abstract View createView(LayoutInflater inflater);

	/**
	 * using the holed views to display data
	 */
	public abstract void showData(ItemDataType itemData);

	public void setItemData(ItemDataType itemData) {
		mItemData = mLastItemData;
		mLastItemData = itemData;
	}

	/**
	 * Check if the View Holder is still display the same data after back to screen.
	 * 
	 * A view in a listview or gridview may go down the screen and then back,
	 * 
	 * for efficiency, in getView() method, a convertView will be reused.
	 * 
	 * If the convertView is reused, View Holder will hold new data.
	 * 
	 */
	public boolean stillHoldLastItemData() {
		return mItemData != null && mLastItemData != null && mLastItemData == mItemData;
	}

	/**
	 * return the data assigned currently
	 * 
	 * @return
	 */
	public ItemDataType getItemData() {
		return mItemData;
	}

}