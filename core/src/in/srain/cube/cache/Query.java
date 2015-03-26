package in.srain.cube.cache;

import android.text.TextUtils;
import in.srain.cube.request.JsonData;

public class Query<T> implements ICacheAble<T> {

    public enum RequestType {
        USE_CACHE_NOT_EXPIRED,
        USE_DATA_CREATED,
        USE_CACHE_ANYWAY,
        FAIL,
    }

    private QueryHandler mHandler;
    private CacheManager mCacheManager;
    private long mCacheTime = 86400;
    private String mCacheKey;
    private boolean mUseCacheAnyway = false;
    private boolean mDisable = false;
    private String mInitAssertPath = null;

    public Query(CacheManager cacheManager) {
        mCacheManager = cacheManager;
    }

    public void continueAfterCreateData(final String data) {
        if (!TextUtils.isEmpty(data)) {
            mCacheManager.continueAfterCreateData(this, data);
        } else {
            queryFail();
        }
    }

    public Query<T> setCacheTime(long time) {
        mCacheTime = time;
        return this;
    }

    private void queryFail() {
        mHandler.onQueryFinish(RequestType.FAIL, null, true);
    }

    public <T> void setHandler(QueryHandler<T> handler) {
        mHandler = handler;
    }

    public void query() {
        mCacheManager.requestCache(this);
    }

    public T querySync() {
        return mCacheManager.requestCacheSync(this);
    }

    @Override
    public long getCacheTime() {
        return mCacheTime;
    }

    @Override
    public String getCacheKey() {
        return mCacheKey;
    }

    @Override
    public Query<T> setCacheKey(String key) {
        mCacheKey = key;
        return this;
    }

    @Override
    public Query<T> setUseCacheAnyway(boolean use) {
        mUseCacheAnyway = use;
        return this;
    }

    @Override
    public boolean useCacheAnyway() {
        return mUseCacheAnyway;
    }

    @Override
    public Query<T> setAssertInitDataPath(String path) {
        mInitAssertPath = path;
        return this;
    }

    @Override
    public String getAssertInitDataPath() {
        return mInitAssertPath;
    }

    @Override
    public T processRawDataFromCache(JsonData rawData) {
        if (mHandler != null) {
            return (T) mHandler.processRawDataFromCache(rawData);
        }
        return null;
    }

    @Override
    public void onCacheData(CacheResultType cacheResultType, T cacheData, boolean outOfDate) {
        switch (cacheResultType) {
            case FROM_CACHE_FILE:
                break;
            case FROM_INIT_FILE:
                break;
            case FROM_MEMORY:
                break;
            case FROM_CREATED:
                break;
        }

        if (outOfDate) {
            if (mHandler != null && mUseCacheAnyway) {
                mHandler.onQueryFinish(RequestType.USE_CACHE_ANYWAY, cacheData, outOfDate);
            }
        } else {
            switch (cacheResultType) {
                case FROM_CACHE_FILE:
                    break;
                case FROM_INIT_FILE:
                    break;
                case FROM_MEMORY:
                    break;
                case FROM_CREATED:
                    break;
            }
            if (mHandler != null) {
                mHandler.onQueryFinish(RequestType.USE_CACHE_NOT_EXPIRED, cacheData, true);
            }
        }
    }

    @Override
    public void onNoCacheData(CacheManager cacheManager) {
        if (mHandler != null) {
            continueAfterCreateData(mHandler.createDataForCache(this));
        } else {
            queryFail();
        }
    }

    @Override
    public Query<T> setDisableCache(boolean disable) {
        mDisable = disable;
        return this;
    }

    @Override
    public boolean cacheIsDisabled() {
        return mHandler != null && mDisable;
    }
}
