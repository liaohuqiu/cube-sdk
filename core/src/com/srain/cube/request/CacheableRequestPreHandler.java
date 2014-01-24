package com.srain.cube.request;

public interface CacheableRequestPreHandler extends BeforeRequestHandler {

	/**
	 * Once the cache key is sepified, the data will be cached by using this key,
	 * 
	 * or else, the url path return by getRequestUrl() will be used, after '/' has been replaced into '-'.
	 */
	public String getSpecificCacheKey();

	public String getInitFileAssertPath();

	/**
	 * Indicate how long the data should be cached
	 * 
	 * @return
	 */
	public int getCacheTime();
}