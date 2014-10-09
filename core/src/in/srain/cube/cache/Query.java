package in.srain.cube.cache;

import android.text.TextUtils;
import in.srain.cube.request.JsonData;

public class Query<T> implements ICacheAble<T> {

    private QueryHandler mHandler;
    private CacheManager mCacheManager;

    public Query(CacheManager cacheManager) {
        mCacheManager = cacheManager;
    }

    public void continueAfterCreateData(final String data) {
        if (!TextUtils.isEmpty(data)) {
            mCacheManager.continueAfterCreateData(this, data);
        }
    }

    public <T> void setHandler(QueryHandler<T> handler) {
        mHandler = handler;
    }

    public void query() {
        mCacheManager.requestCache(this);
    }

    @Override
    public int getCacheTime() {
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
    public void onCacheData(T cacheData, boolean outOfDate) {
        if (outOfDate) {
            if (mHandler != null && mHandler.useCacheAnyway()) {
                mHandler.onQueryFinish(cacheData, outOfDate);
            }
        } else {
            mHandler.onQueryFinish(cacheData, true);
        }
    }

    @Override
    public void createDataForCache(CacheManager cacheManager) {
        if (mHandler != null) {
            continueAfterCreateData(mHandler.createDataForCache(this));
        } else {

        }
    }

    @Override
    public boolean disableCache() {
        return mHandler != null && mHandler.disableCache();
    }
}
