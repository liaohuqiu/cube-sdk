package in.srain.cube.cache;

import android.content.Context;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import in.srain.cube.concurrent.SimpleExecutor;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.file.FileUtil;
import in.srain.cube.file.LruFileCache;
import in.srain.cube.request.JsonData;
import in.srain.cube.util.CLog;

/**
 * @author http://www.liaohuqiu.net
 */
public class CacheManager {

    private static final boolean DEBUG = CLog.DEBUG_CACHE;
    private static final String LOG_TAG = "cube_cache";

    private LruCache<String, CacheInfo> mMemoryCache;
    private LruFileCache mFileCache;

    private static final int AFTER_READ_FROM_FILE = 0x01;
    private static final int AFTER_READ_FROM_ASSERT = 0x02;
    private static final int AFTER_CONVERT = 0x04;

    private static final int DO_READ_FROM_FILE = 0x01;
    private static final int DO_READ_FROM_ASSERT = 0x02;
    private static final int DO_CONVERT = 0x04;

    private Context mContext;

    public CacheManager(Context content, String cacheDir, int memoryCacheSizeInKB, int fileCacheSizeInKB) {
        mContext = content;

        mMemoryCache = new LruCache<String, CacheInfo>(memoryCacheSizeInKB * 1024) {
            @Override
            protected int sizeOf(String key, CacheInfo value) {
                return (value.getSize() + key.getBytes().length);
            }
        };
        mFileCache = new LruFileCache(content, cacheDir, fileCacheSizeInKB * 1024);
        mFileCache.initDiskCacheAsync();
        if (DEBUG) {
            CLog.d(LOG_TAG, "init file cache. dir: %s => %s, size: %s, used: %s", cacheDir, mFileCache.getCachePath(), mFileCache.getMaxSize(), mFileCache.getUsedSpace());
        }
    }

    public <T> void requestCache(ICacheAble<T> cacheAble) {
        InnerCacheTask<T> task = new InnerCacheTask<T>(cacheAble);
        task.beginQuery();
    }

    public <T> void continueAfterCreateData(ICacheAble<T> cacheAble, final String data) {
        setCacheData(cacheAble, data);
        InnerCacheTask<T> task = new InnerCacheTask<T>(cacheAble);
        task.beginConvertDataAsync();
    }

    public <T> void setCacheData(final ICacheAble<T> cacheAble, final String data) {
        if (cacheAble.disableCache() || TextUtils.isEmpty(data)) {
            return;
        }
        setCacheData(cacheAble.getCacheKey(), data);
    }

    public void setCacheData(final String cacheKey, final String data) {
        if (TextUtils.isEmpty(cacheKey) || TextUtils.isEmpty(data)) {
            return;
        }
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, setCacheData", cacheKey);
        }
        SimpleExecutor.getInstance().execute(

                new Runnable() {
                    @Override
                    public void run() {
                        CacheInfo cacheInfo = CacheInfo.create(data);
                        putDataToMemoryCache(cacheKey, cacheInfo);
                        mFileCache.write(cacheKey, cacheInfo.getCacheData());
                        mFileCache.flushDiskCacheAsyncWithDelay(1000);
                    }
                }
        );
    }

    private class InnerCacheTask<T1> extends SimpleTask {

        private ICacheAble<T1> mCacheAble;

        private CacheInfo mRawData;
        private T1 mResult;
        private int mWorkType = 0;
        private int mCurrentStatus = 0;

        public InnerCacheTask(ICacheAble<T1> cacheAble) {
            mCacheAble = cacheAble;
        }

        void beginQuery() {

            if (mCacheAble.disableCache()) {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "%s, Cache is disabled, query from server", mCacheAble.getCacheKey());
                }
                mCacheAble.createDataForCache(CacheManager.this);
                return;
            }

            String cacheKey = mCacheAble.getCacheKey();

            // try to find in runtime cache
            mRawData = mMemoryCache.get(cacheKey);
            if (mRawData != null) {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "%s, exist in list", mCacheAble.getCacheKey());
                }
                beginConvertDataAsync();
                return;
            }

            // try read from cache data
            boolean hasFileCache = mFileCache.has(mCacheAble.getCacheKey());
            if (hasFileCache) {
                beginQueryFromCacheFileAsync();
                return;
            }

            // try to read from assert cache file
            String assertInitDataPath = mCacheAble.getAssertInitDataPath();
            if (assertInitDataPath != null && assertInitDataPath.length() > 0) {
                beginQueryFromAssertCacheFileAsync();
                return;
            }

            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, cache file not exist", mCacheAble.getCacheKey());
            }
            mCacheAble.createDataForCache(CacheManager.this);
        }

        @Override
        public void doInBackground() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, doInBackground: mWorkType: %s", mCacheAble.getCacheKey(), mWorkType);
            }
            switch (mWorkType) {

                case DO_READ_FROM_FILE:
                    doQueryFromCacheFileInBackground();
                    break;

                case DO_READ_FROM_ASSERT:
                    doQueryFromAssertCacheFileInBackground();
                    break;

                case DO_CONVERT:
                    doConvertDataInBackground();
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onFinish() {
            switch (mCurrentStatus) {
                case AFTER_READ_FROM_FILE:
                case AFTER_READ_FROM_ASSERT:
                    beginConvertDataAsync();
                    break;

                case AFTER_CONVERT:
                    done();
                    break;

                default:
                    break;
            }
        }

        private void beginQueryFromCacheFileAsync() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, beginQueryFromCacheFileAsync", mCacheAble.getCacheKey());
            }
            mWorkType = DO_READ_FROM_FILE;
            restart();
            SimpleExecutor.getInstance().execute(this);
        }

        private void beginQueryFromAssertCacheFileAsync() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, beginQueryFromAssertCacheFileAsync", mCacheAble.getCacheKey());
            }
            mWorkType = DO_READ_FROM_ASSERT;
            restart();
            SimpleExecutor.getInstance().execute(this);
        }

        private void beginConvertDataAsync() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, beginConvertDataAsync", mCacheAble.getCacheKey());
            }
            mWorkType = DO_CONVERT;
            restart();
            SimpleExecutor.getInstance().execute(this);
        }

        private void doQueryFromCacheFileInBackground() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, try read cache data from file", mCacheAble.getCacheKey());
            }

            String cacheContent = mFileCache.read(mCacheAble.getCacheKey());
            JsonData jsonData = JsonData.create(cacheContent);
            mRawData = CacheInfo.create(jsonData.optString("data"), jsonData.optInt("time"));

            setCurrentStatus(AFTER_READ_FROM_FILE);
        }

        private void doQueryFromAssertCacheFileInBackground() {

            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, try read cache data from assert file", mCacheAble.getCacheKey());
            }

            String cacheContent = FileUtil.readAssert(mContext, mCacheAble.getAssertInitDataPath());
            mRawData = CacheInfo.create(cacheContent, -2);
            putDataToMemoryCache(mCacheAble.getCacheKey(), mRawData);

            setCurrentStatus(AFTER_READ_FROM_ASSERT);
        }

        private void doConvertDataInBackground() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, doConvertDataInBackground", mCacheAble.getCacheKey());
            }
            JsonData data = JsonData.create(mRawData.data);
            mResult = mCacheAble.processRawDataFromCache(data);
            setCurrentStatus(AFTER_CONVERT);
        }

        private void setCurrentStatus(int status) {
            mCurrentStatus = status;
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, setCurrentStatus: %s", mCacheAble.getCacheKey(), status);
            }
        }

        private void done() {

            int lastTime = mRawData.time;
            long timeInterval = System.currentTimeMillis() / 1000 - lastTime;
            boolean outOfDate = timeInterval > mCacheAble.getCacheTime() || timeInterval < 0;
            mCacheAble.onCacheData(mResult, outOfDate);
            if (outOfDate) {
                mCacheAble.createDataForCache(CacheManager.this);
            }
        }
    }

    private void putDataToMemoryCache(String key, CacheInfo data) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, set cache to runtime cache list", key);
        }
        mMemoryCache.put(key, data);
    }

    /**
     * delete cache by key
     *
     * @param key
     */
    public void invalidateCache(String key) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, invalidateCache", key);
        }
        mFileCache.delete(key);
        mMemoryCache.remove(key);
    }

    /**
     * clear the memory cache
     */
    public void clearMemoryCache() {
        if (mMemoryCache != null) {
            mMemoryCache.evictAll();
        }
    }

    /**
     * get the spaced has been used
     *
     * @return
     */
    public int getMemoryCacheUsedSpace() {
        return mMemoryCache.size();
    }

    /**
     * get the spaced max space in config
     *
     * @return
     */
    public int getMemoryCacheMaxSpace() {
        return mMemoryCache.maxSize();
    }

    /**
     * clear the disk cache
     */
    public void clearDiskCache() {
        if (null != mFileCache) {
            mFileCache.clearCache();
        }
    }

    /**
     * return the file cache path
     *
     * @return
     */
    public String getFileCachePath() {
        if (null != mFileCache) {
            return mFileCache.getCachePath();
        }
        return null;
    }

    /**
     * get the used space in file cache
     *
     * @return
     */
    public long getFileCacheUsedSpace() {
        return null != mFileCache ? mFileCache.getUsedSpace() : 0;
    }

    /**
     * get the max space for file cache
     *
     * @return
     */
    public long getFileCacheMaxSpace() {
        if (null != mFileCache) {
            return mFileCache.getMaxSize();
        }
        return 0;
    }
}