package in.srain.cube.cache;

import in.srain.cube.request.JsonData;

/**
 * Describe the behaviour of a object who can be cached
 */
public interface ICacheAble<T> {

    /**
     * In seconds
     *
     * @return
     */
    public long getCacheTime();

    public String getCacheKey();

    /**
     * file path under /res, For example: "/cache_init/test.json";
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

    /**
     * temporarily disable cache. The data will no be load from cache and will also not put into cache
     *
     * @return
     */
    public boolean cacheIsDisabled();
}
