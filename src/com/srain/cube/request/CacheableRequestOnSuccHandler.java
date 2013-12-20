package com.srain.cube.request;

/**
 * This interface indicates that you can do something when the cache data if found.
 */
public interface CacheableRequestOnSuccHandler extends RequestOnSuccHandler {

	/**
	 * the data from cache, outoufDate detective if the data is out of date.
	 * 
	 * @param cacheData
	 */
	public void onCacheData(JsonData cacheData, boolean outoufDate);
}