package in.srain.cube.request;

import android.text.TextUtils;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.util.CLog;

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

    protected static final boolean DEBUG = CLog.DEBUG_REQUEST_CACHE;
    protected static final String LOG_TAG = "cube_request_cache";

    private CacheAbleRequestHandler<T> mHandler;
    private CacheAbleRequestPrePreHandler mPreHandler;

    private T mCacheData;
    private boolean mOutOfDate;
    private String mCacheKey = null;

    private int mTimeout = 0;
    private boolean mHasTimeout = false;
    private boolean mUsingCacheAnyway = false;
    private boolean mHasNotified = false;

    public CacheAbleRequest(CacheAbleRequestPrePreHandler preHandler, final CacheAbleRequestHandler<T> handler) {
        mPreHandler = preHandler;
        mHandler = handler;
    }

    // ===========================================================
    // Override parent
    // ===========================================================
    @Override
    public void doSendRequest() {
        RequestCacheManager.getInstance().requestCache(this);
    }

    @Override
    public void setTimeout(int timeOut) {
        mTimeout = timeOut;
    }

    @Override
    public void usingCacheAnyway(boolean use) {
        mUsingCacheAnyway = use;
    }

    @Override
    public void onRequestSuccess(T data) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onRequestSuccess", getCacheKey());
        }
        if (hasBeenCanceled()) {
            return;
        }
        mCacheData = data;
        if (null != mHandler) {
            mHandler.onRequestFinish(data);
            if (!mHasTimeout && !mUsingCacheAnyway) {
                notifyRequestFinish(ResultType.USE_DATA_FROM_SERVER, false);
            } else {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "%s, will not notifyRequestFinish", getCacheKey());
                }
            }
        }
    }

    @Override
    public void prepareRequest() {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, prepareRequest", getCacheKey());
        }
        if (mPreHandler != null) {
            mPreHandler.prepareRequest(this);
        }
    }

    @Override
    public void onRequestFail(FailData failData) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onRequestFail", getCacheKey());
        }
        if (hasBeenCanceled()) {
            return;
        }
        if (null != mHandler) {
            mHandler.onRequestFail(failData);
            if (mCacheData != null && !disableCache() && !mUsingCacheAnyway) {
                notifyRequestFinish(ResultType.USE_CACHE_ON_FAIL, true);
            } else {
                mHandler.onRequestFail(null);
            }
        }
    }

    @Override
    public void queryFromServer() {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, queryFromServer", getCacheKey());
        }
        if (hasBeenCanceled()) {
            return;
        }
        doQueryFromServer();
        beginTimeout();
    }

    protected void doQueryFromServer() {
        SimpleRequestManager.sendRequest(this);
    }

    @Override
    public boolean disableCache() {
        if (mPreHandler != null) {
            return mPreHandler.disableCache();
        }
        return false;
    }

    // ===========================================================
    // Implements Interface {@link ICacheAble}
    // ===========================================================
    @Override
    public void onCacheData(T data, boolean outOfDate) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onCacheData, out of date: %s", getCacheKey(), outOfDate);
        }
        if (hasBeenCanceled()) {
            return;
        }
        mCacheData = data;
        mOutOfDate = outOfDate;
        if (null != mHandler) {
            mHandler.onCacheData(data, outOfDate);

            if (mUsingCacheAnyway) {
                notifyRequestFinish(ResultType.USE_CACHE_ANYWAY, mOutOfDate);
            } else {
                if (!outOfDate) {
                    notifyRequestFinish(ResultType.USE_CACHE_NOT_EXPIRED, false);
                }
            }
        }
    }

    @Override
    public int getCacheTime() {
        return mPreHandler.getCacheTime();
    }

    @Override
    public String getCacheKey() {
        if (disableCache()) {
            throw new RuntimeException("getCacheKey() should not be called, I should check the code.");
        }
        if (mCacheKey == null) {
            String cacheKey = mPreHandler.getSpecificCacheKey();
            if (TextUtils.isEmpty(cacheKey)) {

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
                    e.printStackTrace();
                }
            }
            mCacheKey = cacheKey;
        }
        return mCacheKey;
    }

    @Override
    public String getAssertInitDataPath() {
        return mPreHandler.getInitFileAssertPath();
    }

    @Override
    public T onDataFromServer(String data) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, onDataFromServer", getCacheKey());
        }
        // cache the data
        if (!TextUtils.isEmpty(data) && !disableCache()) {
            RequestCacheManager.getInstance().cacheRequest(this, data);
        }
        return super.onDataFromServer(data);
    }

    @Override
    public T processOriginDataFromServer(JsonData rawData) {
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
    private void notifyRequestFinish(ResultType type, boolean outOfDate) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, notifyRequestFinish: %s, %s", getCacheKey(), type, outOfDate);
        }
        if (mHasNotified) {
            return;
        }
        mHasNotified = true;
        mHandler.onCacheAbleRequestFinish(mCacheData, type, outOfDate);
    }

    private void timeout() {
        mHasTimeout = true;
        if (mCacheData != null && mHandler != null) {
            notifyRequestFinish(ResultType.USE_CACHE_ON_TIMEOUT, true);
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
}
