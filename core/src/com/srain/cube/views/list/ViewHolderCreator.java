package com.srain.cube.views.list;

/**
 * A interface that defines what a View Holder Creator should do.
 * 
 * @author http://www.liaohuqiu.net
 * 
 * @param <ItemDataType>
 *            the generic type of the data in each item of a list.
 */
public interface ViewHolderCreator<ItemDataType> {
	public ViewHolderBase<ItemDataType> createViewHodler();
}