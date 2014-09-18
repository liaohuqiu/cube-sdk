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

    private static final boolean DEBUG = CLog.DEBUG_REQUEST_CACHE;
    private static final String LOG_TAG = "cube_request_cache";

    private static final String THREAD_NAME = "Cube-Request-Cache";

    private static CacheManager mInstance;

    private LruCache<String, CacheData> mMemoryCache;
    private LruFileCache mFileCache;

    private static final int AFTER_READ_FROM_FILE = 0x01;
    private static final int AFTER_READ_FROM_ASSERT = 0x02;
    private static final int AFTER_CONVERT = 0x04;

    private static final int DO_READ_FROM_FILE = 0x01;
    private static final int DO_READ_FROM_ASSERT = 0x02;
    private static final int DO_CONVERT = 0x04;

    private Context mContext;

    private CacheManager() {
    }

    /**
     * init cache
     */
    public void init(Context content, String cacheDir) {
        init(content, cacheDir, 1024 * 10, 1024 * 10);
    }

    public void init(Context content, String cacheDir, int memoryCacheSizeInKB, int fileCacheSizeInKB) {
        mContext = content;

        mMemoryCache = new LruCache<String, CacheData>(memoryCacheSizeInKB * 1024) {
            @Override
            protected int sizeOf(String key, CacheData value) {
                return (value.getSize() + key.getBytes().length);
            }
        };
        mFileCache = new LruFileCache(content, cacheDir, fileCacheSizeInKB * 1024);
        mFileCache.initDiskCacheAsync();
        if (DEBUG) {
            CLog.d(LOG_TAG, "init file cache. dir: %s => %s, size: %s, used: %s", cacheDir, mFileCache.getCachePath(), mFileCache.getMaxSize(), mFileCache.getUsedSpace());
        }
    }

    public static CacheManager getInstance() {
        if (null == mInstance)
            mInstance = new CacheManager();
        return mInstance;
    }

    public <T> void requestCache(ICacheAble<T> cacheAbleRequest) {
        ReadCacheTask<T> task = new ReadCacheTask<T>(cacheAbleRequest);
        task.beginQuery();
    }

    public void cacheData(final String cacheKey, final String data) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, cacheData", cacheKey);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                CacheData cacheData = CacheData.create(data);
                setCacheData(cacheKey, cacheData);
                mFileCache.write(cacheKey, cacheData.getCacheData());
            }
        }, THREAD_NAME).start();
    }

    public <T> void cacheData(final ICacheAble<T> cacheAble, final String data) {
        cacheData(cacheAble.getCacheKey(), data);
    }

    private class ReadCacheTask<T1> extends SimpleTask {

        private ICacheAble<T1> mCacheAble;

        private CacheData mRawData;
        private T1 mResult;
        private int mWorkType = 0;
        private int mCurrentStatus = 0;

        public ReadCacheTask(ICacheAble<T1> cacheAble) {
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
            mRawData = CacheData.create(jsonData.optString("data"), jsonData.optInt("time"));

            setCurrentStatus(AFTER_READ_FROM_FILE);
        }

        private void doQueryFromAssertCacheFileInBackground() {

            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, try read cache data from assert file", mCacheAble.getCacheKey());
            }

            String cacheContent = FileUtil.readAssert(mContext, mCacheAble.getAssertInitDataPath());
            mRawData = CacheData.create(cacheContent, -2);
            setCacheData(mCacheAble.getCacheKey(), mRawData);

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

    private void setCacheData(String key, CacheData data) {
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