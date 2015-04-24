package in.srain.cube.request;

import android.text.TextUtils;
import in.srain.cube.cache.CacheManager;
import in.srain.cube.cache.CacheResultType;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;

import java.net.URI;
import java.net.URISyntaxException;

public class CacheAbleRequest<T> extends RequestBase<T> implements ICacheAbleRequest<T> {

    public static enum ResultType {
        USE_CACHE_NOT_EXPIRED,
        USE_CACHE_ANYWAY,
        USE_CACHE_ON_TIMEOUT,
        USE_DATA_FROM_SERVER,
        USE_CACHE_ON_FAIL,
    }

    protected static final boolean DEBUG = CubeDebug.DEBUG_CACHE;
    protected static final String LOG_TAG = "cube-cache-request";

    private CacheAbleRequestHandler<T> mHandler;

    private T mCacheData;
    private boolean mOutOfDate;
    private String mCacheKey = null;

    private int mTimeout = 0;
    private boolean mHasTimeout = false;
    private boolean mUseCacheAnyway = false;
    private boolean mHasNotified = false;
    protected boolean mForceQueryFromServer = false;

    private String mInitDataPath;
    private boolean mDisableCache = false;
    private long mCacheTime;

    public CacheAbleRequest() {
    }

    public CacheAbleRequest(final CacheAbleRequestHandler<T> handler) {
        setCacheAbleRequestHandler(handler);
    }

    public void setCacheAbleRequestHandler(CacheAbleRequestHandler<T> handler) {
        mHandler = handler;
    }

    public void forceQueryFromServer(boolean force) {
        mForceQueryFromServer = force;
    }

    // ===========================================================
    // Override parent
    // ===========================================================
    @Override
    public void doSendRequest() {
        RequestCacheManager.getInstance().requestCache(this);
    }

    /**
     * Timeout will not be considerate
     *
     * @return
     */
    @Override
    protected T doRequestSync() {
        T data = RequestCacheManager.getInstance().requestCacheSync(this);
        if (data == null) {
            data = RequestManager.getInstance().getRequestProxy(this).requestSync(this);
        }
        return data;
    }

    /**
     * prepare request
     */
    @Override
    protected void prepareRequest() {
        RequestManager.getInstance().getRequestProxy(this).prepareRequest(this);
    }

    // ===========================================================
    // Override Interface
    // ===========================================================
    @Override
    public void setTimeout(int timeOut) {
        mTimeout = timeOut;
    }

    @Override
    public CacheAbleRequest<T> setUseCacheAnyway(boolean use) {
        mUseCacheAnyway = use;
        return this;
    }

    @Override
    public boolean useCacheAnyway() {
        return mUseCacheAnyway;
    }

    @Override
    public void onRequestSuccess(T data) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onRequestSuccess", getCacheKey());
        }
        if (hasBeenCanceled()) {
            return;
        }
        if (null != mHandler) {
            mHandler.onRequestFinish(data);

            // cache data is not available or
            // cache is available and time duration not reach timeout or not always use the cache
            if (mCacheData == null || (!mHasTimeout && !mUseCacheAnyway)) {
                notifyRequestFinish(ResultType.USE_DATA_FROM_SERVER, data, false);
            } else {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "%s, will not notifyRequestFinish", getCacheKey());
                }
            }
        }
    }

    @Override
    public void onRequestFail(FailData failData) {
        RequestManager.getInstance().getRequestProxy(this).onRequestFail(this, failData);
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onRequestFail", getCacheKey());
        }
        if (hasBeenCanceled()) {
            return;
        }
        if (null != mHandler) {
            mHandler.onRequestFail(failData);
            if (mCacheData != null && !cacheIsDisabled() && !mUseCacheAnyway) {
                notifyRequestFinish(ResultType.USE_CACHE_ON_FAIL, mCacheData, true);
            }
        }
    }

    @Override
    public void onNoCacheData(CacheManager cacheManager) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onNoCacheData", getCacheKey());
        }
        if (hasBeenCanceled()) {
            return;
        }
        doQueryFromServer();
        beginTimeout();
    }

    protected void doQueryFromServer() {
        RequestManager.getInstance().getRequestProxy(this).sendRequest(this);
    }

    protected boolean cacheRequestResult() {
        return mForceQueryFromServer || !cacheIsDisabled();
    }

    @Override
    public boolean cacheIsDisabled() {
        if (mForceQueryFromServer) {
            return true;
        }
        return mDisableCache;
    }

    // ===========================================================
    // Implements Interface {@link ICacheAble}
    // ===========================================================
    @Override
    public void onCacheData(CacheResultType cacheResultType, T data, boolean outOfDate) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onQueryFinish, out of date: %s", getCacheKey(), outOfDate);
        }
        if (hasBeenCanceled()) {
            return;
        }
        mCacheData = data;
        mOutOfDate = outOfDate;
        if (mHandler != null) {
            mHandler.onCacheData(data, outOfDate);

            if (mUseCacheAnyway) {
                notifyRequestFinish(ResultType.USE_CACHE_ANYWAY, data, mOutOfDate);
            } else {
                if (!outOfDate) {
                    notifyRequestFinish(ResultType.USE_CACHE_NOT_EXPIRED, data, false);
                }
            }
        }
    }

    @Override
    public long getCacheTime() {
        return mCacheTime;
    }

    @Override
    public String getCacheKey() {
        if (mCacheKey == null) {
            String cacheKey = null;
            String url = getRequestData().getRequestUrl();
            try {
                URI uri = null;
                uri = new URI(url);
                cacheKey = uri.getPath();
                if (cacheKey.startsWith("/")) {
                    cacheKey = cacheKey.substring(1);
                }
                cacheKey = cacheKey.replace("/", "-");
            } catch (URISyntaxException e) {
                if (CubeDebug.DEBUG_REQUEST) {
                    e.printStackTrace();
                }
            }
            if (TextUtils.isEmpty(cacheKey)) {
                throw new RuntimeException("Cache key is null");
            }
            mCacheKey = cacheKey;
        }
        return mCacheKey;
    }

    @Override
    public String getAssertInitDataPath() {
        return mInitDataPath;
    }

    @Override
    public T onDataFromServer(String data) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onDataFromServer", getCacheKey());
        }

        T ret = super.onDataFromServer(data);

        // cache the data
        if (!TextUtils.isEmpty(data) && ret != null && cacheRequestResult()) {
            RequestCacheManager.getInstance().setCacheData(this.getCacheKey(), data);
        }
        return ret;
    }

    @Override
    public T processOriginDataFromServer(JsonData rawData) {
        rawData = RequestManager.getInstance().getRequestProxy(this).processOriginDataFromServer(this, rawData);
        return mHandler.processOriginData(rawData);
    }

    @Override
    public T processRawDataFromCache(JsonData rawData) {
        return mHandler.processOriginData(rawData);
    }

    /**
     * will only notify once
     *
     * @param type
     * @param outOfDate
     */
    private void notifyRequestFinish(ResultType type, T cacheData, boolean outOfDate) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, notifyRequestFinish: %s, %s", getCacheKey(), type, outOfDate);
        }
        if (mHasNotified) {
            return;
        }
        mHasNotified = true;
        mHandler.onCacheAbleRequestFinish(cacheData, type, outOfDate);
    }

    private void timeout() {
        mHasTimeout = true;
        if (mCacheData != null && mHandler != null) {
            notifyRequestFinish(ResultType.USE_CACHE_ON_TIMEOUT, mCacheData, true);
        }
    }

    private void beginTimeout() {
        if (mTimeout > 0 && mCacheData != null) {
            SimpleTask.postDelay(new Runnable() {
                @Override
                public void run() {
                    timeout();
                }
            }, mTimeout);
        }
    }

    @Override
    public CacheAbleRequest<T> setCacheKey(String cacheKey) {
        mCacheKey = cacheKey;
        return this;
    }

    @Override
    public CacheAbleRequest<T> setDisableCache(boolean disable) {
        mDisableCache = disable;
        return this;
    }

    @Override
    public CacheAbleRequest<T> setAssertInitDataPath(String path) {
        mInitDataPath = path;
        return this;
    }

    @Override
    public CacheAbleRequest<T> setCacheTime(long time) {
        mCacheTime = time;
        return this;
    }
}
