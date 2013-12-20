package com.srain.cube.request;

/**
 * 
 * Define what should be done before send a cacheable request.
 * 
 * @author huqiu.lhq
 * 
 */
public interface CacheableRequestPreHandler extends RequestPreHandler {

	/**
	 * Once the cache key is sepified, the data will be cached by using this key,
	 * 
	 * or else, the url path return by getRequestUrl() will be used, after '/' has been replaced into '-'.
	 */
	public String getSpecificCacheKey();

	/**
	 * Specify the path of the initial data file.
	 */
	public String getInitFileAssertPath();

	/**
	 * Indicates how long the data should be cached
	 */
	public int getCacheTime();
}