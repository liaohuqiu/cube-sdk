package com.srain.cube.request;

public interface CacheableRequestOnSuccHandler extends RequestOnSuccHandler {

	/**
	 * the data from cache, outoufDate detective if the data is out of date.
	 * 
	 * @param cacheData
	 */
	public void onCacheData(JsonData cacheData, boolean outoufDate);
}