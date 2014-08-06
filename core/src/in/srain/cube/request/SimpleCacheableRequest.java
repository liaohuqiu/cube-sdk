package in.srain.cube.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.text.TextUtils;
import in.srain.cube.request.RequestCache.ICacheable;

public class SimpleCacheableRequest<T> extends SimpleRequest<T> implements ICacheable<T> {

    private CacheableRequestSuccHandler<T> mCacheableRequestHandler;
    private CacheableRequestPreHandler mCacheableRequestPreHandler;

    public SimpleCacheableRequest(CacheableRequestPreHandler cacheableRequestPreHandler, CacheableRequestSuccHandler<T> cacheableRequestHandler) {
        super(cacheableRequestPreHandler, cacheableRequestHandler);
        mCacheableRequestPreHandler = cacheableRequestPreHandler;
        mCacheableRequestHandler = cacheableRequestHandler;
    }

    public void send() {
        setBeforeRequest();
        RequestCache.getInstance().requestCache(this);
    }

    // ===========================================================
    // Implements Interface @ICacheable
    // ===========================================================
    @Override
    public void onNoCacheDataAvailable() {
        doQuery();
    }

    @Override
    public void onCacheData(T data, boolean outoufDate) {
        mCacheableRequestHandler.onCacheData(data, outoufDate);
        if (outoufDate) {
            doQuery();
        }
    }

    @Override
    public int getCacheTime() {
        return mCacheableRequestPreHandler.getCacheTime();
    }

    @Override
    public String getCacheKey() {
        String cacheKey = mCacheableRequestPreHandler.getSpecificCacheKey();
        if (cacheKey != null && cacheKey.length() > 0)
            return cacheKey;

        String url = getRequestUrl();
        try {
            URI uri = null;
            uri = new URI(url);
            cacheKey = uri.getPath();
            if (cacheKey.startsWith("/"))
                cacheKey = cacheKey.substring(1);
            cacheKey = cacheKey.replace("/", "-");
            return cacheKey;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return md5(url);
    }

    public static final String md5(final String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getAssertInitDataPath() {
        return mCacheableRequestPreHandler.getInitFileAssertPath();
    }

    @Override
    public T processOriginData(JsonData rawData) {
        // cache the data
        if (rawData != null && rawData.getRawData() != null && rawData.length() > 0 && this.getCacheTime() > 0 && !TextUtils.isEmpty(this.getCacheKey())) {
            RequestCache.getInstance().cacheRequest(this, rawData);
        }
        return super.processOriginData(rawData);
    }

    @Override
    public T processRawDataFromCache(JsonData jsonData) {
        return super.processOriginData(jsonData);
    }

}
