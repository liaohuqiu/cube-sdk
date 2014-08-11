package in.srain.cube.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;

public class CacheAbleRequest<T> extends RequestBase<T> implements RequestCache.ICacheAbleRequest<T> {

    private CacheAbleRequestHandler<T> mCacheAbleRequestHandler;
    private CacheAbleRequestPreHandler mCacheAbleRequestPreHandler;
    private T mCacheData;
    private boolean mOutOfDate;

    public CacheAbleRequest(CacheAbleRequestPreHandler cacheAbleRequestPreHandler, final CacheAbleRequestHandler<T> handler) {
        mCacheAbleRequestPreHandler = cacheAbleRequestPreHandler;
        mCacheAbleRequestHandler = handler;
    }

    /**
     * Implements interface {@link IRequest}
     */
    @Override
    public void onRequestSuccess(T data) {
        if (null != mCacheAbleRequestHandler) {
            mCacheAbleRequestHandler.onRequestFinish(data);
            mCacheAbleRequestHandler.onCacheAbleRequestFinish(mCacheData, false, mOutOfDate);
        }
    }

    @Override
    public void beforeRequest() {
        if (mCacheAbleRequestPreHandler != null) {
            mCacheAbleRequestPreHandler.beforeRequest(this);
        }
    }

    @Override
    public void onRequestFail(RequestResultType requestResultType) {
        if (null != mCacheAbleRequestHandler) {
            mCacheAbleRequestHandler.onRequestFail(requestResultType);
            mCacheAbleRequestHandler.onCacheAbleRequestFinish(mCacheData, false, mOutOfDate);
        }
    }

    @Override
    public void queryFromServer() {
        SimpleRequestManager.sendRequest(this);
    }

    public void send() {
        RequestCache.getInstance().requestCache(this);
    }

    // ===========================================================
    // Implements Interface @ICacheAble
    // ===========================================================
    @Override
    public void onCacheData(T data, boolean outOfDate) {
        mCacheData = data;
        mOutOfDate = outOfDate;
        if (null != mCacheAbleRequestHandler) {
            mCacheAbleRequestHandler.onCacheData(data, outOfDate);
            if (!outOfDate) {
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
    public T processOriginData(JsonData rawData) {
        // cache the data
        if (rawData != null && rawData.getRawData() != null && rawData.length() > 0 && this.getCacheTime() > 0 && !TextUtils.isEmpty(this.getCacheKey())) {
            RequestCache.getInstance().cacheRequest(this, rawData);
        }
        if (null != mCacheAbleRequestHandler) {
            return mCacheAbleRequestHandler.processOriginData(rawData);
        }
        return null;
    }

    @Override
    public T processRawDataFromCache(JsonData jsonData) {
        return processOriginData(jsonData);
    }

}
