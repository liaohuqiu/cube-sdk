package net.liaohuqiu.cube.request;

public interface CacheableRequestSuccHandler<T1> extends RequestSuccHandler<T1> {

	/**
	 * the data from cache, outoufDate detective if the data is out of date.
	 * 
	 * @param cacheData
	 */
	public void onCacheData(T1 data, boolean outoufDate);
}