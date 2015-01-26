package in.srain.cube.cache;

import in.srain.cube.request.JsonData;

/**
 * Describe the behaviour of a object who can be cached
 */
public interface ICacheAble<T> {

    /**
     * set cache time, in seconds
     *
     * @param time
     * @return
     */
    public ICacheAble<T> setCacheTime(long time);

    /**
     * In seconds
     *
     * @return
     */
    public long getCacheTime();

    public ICacheAble<T> setCacheKey(String key);

    public String getCacheKey();

    /**
     * Using cache data if existent, ignore whether it is expired or not.
     *
     * @param use
     */
    public ICacheAble<T> useCacheAnyway(boolean use);

    /**
     * @param path file path under /res, For example: "/cache_init/test.json";
     * @return
     */
    public ICacheAble<T> setAssertInitDataPath(String path);

    /**
     * @return file path under /res, For example: "/cache_init/test.json";
     */
    public String getAssertInitDataPath();

    /**
     * We need to process the data from data source, do some filter of convert the structure.
     * <p/>
     * As the "Assert Data" is a special data source, we also need to do the same work.
     */
    public T processRawDataFromCache(JsonData rawData);

    /**
     * when data loaded from cache
     *
     * @param cacheData
     * @param outOfDate
     */
    public void onCacheData(CacheResultType cacheResultType, T cacheData, boolean outOfDate);

    /**
     * create data when no cache is available.
     */
    public void createDataForCache(CacheManager cacheManager);

    public ICacheAble<T> setDisableCache(boolean disable);

    /**
     * temporarily disable cache. The data will no be load from cache and will also not be put into cache
     *
     * @return
     */
    public boolean cacheIsDisabled();
}
