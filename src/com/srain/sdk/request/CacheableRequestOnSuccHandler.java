package com.srain.sdk.request;

public interface CacheableRequestOnSuccHandler extends RequestOnSuccHandler {

	/**
	 * the data from cache and which is out of date
	 * 
	 * @param previousJsonData
	 */
	public void onCachedPreviousData(JsonData previousJsonData);
}