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

    private void queryFail() {
        mHandler.onQueryFinish(RequestType.FAIL, null, true);
    }

    public <T> void setHandler(QueryHandler<T> handler) {
        mHandler = handler;
    }

    public void query() {
        mCacheManager.requestCache(this);
    }

    @Override
    public long getCacheTime() {
        if (mHandler != null) {
            return mHandler.getCacheTime();
        }
        return 0;
    }

    @Override
    public String getCacheKey() {
        if (mHandler != null) {
            return mHandler.getCacheKey();
        }
        return null;
    }

    @Override
    public String getAssertInitDataPath() {
        if (mHandler != null) {
            return mHandler.getAssertInitDataPath();
        }
        return null;
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
            if (mHandler != null && mHandler.useCacheAnyway()) {
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
            mHandler.onQueryFinish(RequestType.USE_CACHE_NOT_EXPIRED, cacheData, true);
        }
    }

    @Override
    public void createDataForCache(CacheManager cacheManager) {
        if (mHandler != null) {
            continueAfterCreateData(mHandler.createDataForCache(this));
        } else {
            queryFail();
        }
    }

    @Override
    public boolean cacheIsDisabled() {
        return mHandler != null && mHandler.disableCache();
    }
}
