package in.srain.cube.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;
import in.srain.cube.app.lifecycle.LifeCycleComponent;
import in.srain.cube.util.CLog;

public class CacheAbleRequest<T> extends RequestBase<T> implements RequestCache.ICacheAbleRequest<T> {

    protected static final boolean DEBUG = CLog.DEBUG_REQUEST_CACHE;
    protected static final String LOG_TAG = "cube_request_cache";

    private CacheAbleRequestHandler<T> mCacheAbleRequestHandler;
    private CacheAbleRequestPreHandler mCacheAbleRequestPreHandler;
    private T mCacheData;
    private boolean mOutOfDate;
    private boolean mHasStop = false;

    public CacheAbleRequest(CacheAbleRequestPreHandler cacheAbleRequestPreHandler, final CacheAbleRequestHandler<T> handler) {
        mCacheAbleRequestPreHandler = cacheAbleRequestPreHandler;
        mCacheAbleRequestHandler = handler;
    }

    /**
     * Implements interface {@link IRequest}
     */
    @Override
    public void onRequestSuccess(T data) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "onRequestSuccess %s", getCacheKey());
        }
        if (mHasStop) {
            return;
        }
        if (null != mCacheAbleRequestHandler) {
            mCacheAbleRequestHandler.onRequestFinish(data);
            if (DEBUG) {
                CLog.d(LOG_TAG, "onCacheAbleRequestFinish: %s", getCacheKey());
            }
            mCacheAbleRequestHandler.onCacheAbleRequestFinish(data, false, mOutOfDate);
        }
    }

    @Override
    public void beforeRequest() {
        if (DEBUG) {
            CLog.d(LOG_TAG, "beforeRequest: %s", getCacheKey());
        }
        if (mCacheAbleRequestPreHandler != null) {
            mCacheAbleRequestPreHandler.beforeRequest(this);
        }
    }

    @Override
    public void onRequestFail(RequestResultType requestResultType) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "onRequestFail: %s", getCacheKey());
        }
        if (mHasStop) {
            return;
        }
        if (null != mCacheAbleRequestHandler) {
            mCacheAbleRequestHandler.onRequestFail(requestResultType);
            if (mCacheData != null && !disableCache()) {
                mCacheAbleRequestHandler.onCacheAbleRequestFinish(mCacheData, false, mOutOfDate);
            }
        }
    }

    @Override
    public void queryFromServer() {
        if (DEBUG) {
            CLog.d(LOG_TAG, "queryFromServer: %s", getCacheKey());
        }
        if (mHasStop) {
            return;
        }
        SimpleRequestManager.sendRequest(this);
    }

    @Override
    public boolean disableCache() {
        if (mCacheAbleRequestPreHandler != null) {
            return mCacheAbleRequestPreHandler.disableCache();
        }
        return false;
    }

    public void send() {
        RequestCache.getInstance().requestCache(this);
    }

    // ===========================================================
    // Implements Interface @ICacheAble
    // ===========================================================
    @Override
    public void onCacheData(T data, boolean outOfDate) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "onCacheData: %s", getCacheKey());
        }
        if (mHasStop) {
            return;
        }
        mCacheData = data;
        mOutOfDate = outOfDate;
        if (null != mCacheAbleRequestHandler) {
            mCacheAbleRequestHandler.onCacheData(data, outOfDate);
            if (!outOfDate) {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "onCacheAbleRequestFinish: %s", getCacheKey());
                }
                mCacheAbleRequestHandler.onCacheAbleRequestFinish(data, true, false);
            }
        }
    }

    @Override
    public int getCacheTime() {
        return mCacheAbleRequestPreHandler.getCacheTime();
    }

    @Override
    public String getCacheKey() {
        String cacheKey = mCacheAbleRequestPreHandler.getSpecificCacheKey();
        return cacheKey;
    }

    @Override
    public String getAssertInitDataPath() {
        return mCacheAbleRequestPreHandler.getInitFileAssertPath();
    }

    @Override
    public T processOriginDataFromServer(JsonData rawData) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "processOriginDataFromServer: %s", getCacheKey());
        }
        // cache the data
        if (rawData != null && rawData.getRawData() != null && rawData.length() > 0 && !disableCache()) {
            RequestCache.getInstance().cacheRequest(this, rawData);
        }
        return mCacheAbleRequestHandler.processOriginData(rawData);
    }

    @Override
    public T processRawDataFromCache(JsonData jsonData) {
        return mCacheAbleRequestHandler.processOriginData(jsonData);
    }

    @Override
    public void cancelRequest() {
        mHasStop = true;
    }
}
