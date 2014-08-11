package in.srain.cube.request;

public interface CacheAbleRequestPreHandler extends BeforeRequestHandler {

    /**
     * Once the cache key is specified, the data will be cached by using this key,
     * <p/>
     * or else, the url path return by getRequestUrl() will be used, after '/' has been replaced into '-'.
     */
    public String getSpecificCacheKey();

    public String getInitFileAssertPath();

    public boolean disableCache();

    /**
     * Indicate how long the data should be cached
     *
     * @return
     */
    public int getCacheTime();
}