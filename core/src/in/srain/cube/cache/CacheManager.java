package in.srain.cube.cache;

import android.content.Context;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import in.srain.cube.concurrent.SimpleExecutor;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.request.JsonData;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;

import java.io.IOException;

/**
 * @author http://www.liaohuqiu.net
 */
public class CacheManager {

    private static final boolean DEBUG = CubeDebug.DEBUG_CACHE;
    private static final String LOG_TAG = "cube-cache-manager";

    private static final byte AFTER_READ_FROM_FILE = 0x01;
    private static final byte AFTER_READ_FROM_ASSERT = 0x02;
    private static final byte AFTER_CONVERT = 0x04;

    private static final byte DO_READ_FROM_FILE = 0x01;
    private static final byte DO_READ_FROM_ASSERT = 0x02;
    private static final byte DO_CONVERT = 0x04;

    private static final byte CONVERT_FOR_MEMORY = 0x03;
    private static final byte CONVERT_FOR_FILE = 0x01;
    private static final byte CONVERT_FOR_ASSERT = 0x02;
    private static final byte CONVERT_FOR_CREATE = 0x04;

    private LruCache<String, CacheMetaData> mMemoryCache;
    private DiskCacheProvider mFileCache;
    private Context mContext;

    public CacheManager(Context content, String cacheDir, int memoryCacheSizeInKB, int fileCacheSizeInKB) {
        mContext = content;

        if (TextUtils.isEmpty(cacheDir)) {
            throw new IllegalArgumentException("cacheDir can not be empty");
        }

        if (memoryCacheSizeInKB <= 0) {
            throw new IllegalArgumentException("memoryCacheSizeInKB <= 0");
        }
        if (fileCacheSizeInKB <= 0) {
            throw new IllegalArgumentException("fileCacheSizeInKB <= 0");
        }

        mMemoryCache = new LruCache<String, CacheMetaData>(memoryCacheSizeInKB * 1024) {
            @Override
            protected int sizeOf(String key, CacheMetaData value) {
                return (value.getSize() + key.getBytes().length);
            }
        };


        DiskFileUtils.CacheDirInfo cacheDirInfo = DiskFileUtils.getDiskCacheDir(content, cacheDir, fileCacheSizeInKB, null);
        mFileCache = DiskCacheProvider.createLru(content, cacheDirInfo.path, cacheDirInfo.realSize);

        if (DEBUG) {
            CLog.d(LOG_TAG,
                    "CacheManger: cache dir: %s => %s, size: %s => %s",
                    cacheDir, cacheDirInfo.path, cacheDirInfo.requireSize, cacheDirInfo.realSize);
        }
    }

    public <T> void requestCache(ICacheAble<T> cacheAble) {
        InnerCacheTask<T> task = new InnerCacheTask<T>(cacheAble);
        task.beginQuery();
    }

    public <T> void continueAfterCreateData(ICacheAble<T> cacheAble, final String data) {
        setCacheData(cacheAble.getCacheKey(), data);
        InnerCacheTask<T> task = new InnerCacheTask<T>(cacheAble);
        task.beginConvertDataAsync(CONVERT_FOR_CREATE);
    }

    public void setCacheData(final String cacheKey, final String data) {
        if (TextUtils.isEmpty(cacheKey) || TextUtils.isEmpty(data)) {
            return;
        }
        if (DEBUG) {
            CLog.d(LOG_TAG, "key: %s, setCacheData", cacheKey);
        }
        SimpleExecutor.getInstance().execute(

                new Runnable() {
                    @Override
                    public void run() {
                        CacheMetaData cacheMetaData = CacheMetaData.createForNow(data);
                        putDataToMemoryCache(cacheKey, cacheMetaData);
                        mFileCache.write(cacheKey, cacheMetaData.getCacheData());
                        mFileCache.flushDiskCacheAsyncWithDelay(1000);
                    }
                }
        );
    }

    private void putDataToMemoryCache(String key, CacheMetaData data) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (DEBUG) {
            CLog.d(LOG_TAG, "key: %s, set cache to runtime cache list", key);
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
            CLog.d(LOG_TAG, "key: %s, invalidateCache", key);
        }
        try {
            mFileCache.getDiskCache().delete(key);
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
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
            try {
                mFileCache.getDiskCache().clear();
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * return the file cache path
     *
     * @return
     */
    public String getFileCachePath() {
        if (null != mFileCache) {
            return mFileCache.getDiskCache().getDirectory().getAbsolutePath();
        }
        return null;
    }

    /**
     * get the used space in file cache
     *
     * @return
     */
    public long getFileCacheUsedSpace() {
        return null != mFileCache ? mFileCache.getDiskCache().getSize() : 0;
    }

    /**
     * get the max space for file cache
     *
     * @return
     */
    public long getFileCacheMaxSpace() {
        if (null != mFileCache) {
            return mFileCache.getDiskCache().getCapacity();
        }
        return 0;
    }

    /**
     * Request cache synchronously.
     * If there is not cache data available, return null,
     * and {@link ICacheAble#onNoCacheData} will not no be called.
     *
     * @param cacheAble
     * @param <T>
     * @return if not cache data available, return null, {@link ICacheAble#onNoCacheData} will not no be called.
     */
    @SuppressWarnings({"unused"})
    public <T> T requestCacheSync(ICacheAble<T> cacheAble) {

        if (cacheAble.cacheIsDisabled()) {
            return null;
        }

        // try to find in runtime cache
        String cacheKey = cacheAble.getCacheKey();
        CacheMetaData mRawData = mMemoryCache.get(cacheKey);
        if (mRawData != null) {
            if (DEBUG) {
                CLog.d(LOG_TAG, "key: %s, exist in list", cacheKey);
            }
        }

        // try read from cache data
        if (mRawData == null) {
            boolean hasFileCache = mFileCache.getDiskCache().has(cacheKey);
            if (hasFileCache) {
                String cacheContent = mFileCache.read(cacheKey);
                JsonData jsonData = JsonData.create(cacheContent);
                mRawData = CacheMetaData.createFromJson(jsonData);
            }
        }

        // try to read from assets cache file
        if (mRawData == null) {
            String assertInitDataPath = cacheAble.getAssertInitDataPath();
            if (assertInitDataPath != null && assertInitDataPath.length() > 0) {
                String cacheContent = DiskFileUtils.readAssert(mContext, assertInitDataPath);
                if (!TextUtils.isEmpty(cacheContent)) {
                    mRawData = CacheMetaData.createInvalidated(cacheContent);
                    putDataToMemoryCache(cacheKey, mRawData);
                }
            }
        }

        if (mRawData != null) {
            boolean outOfDate = mRawData.isOutOfDateFor(cacheAble);
            if (outOfDate && !cacheAble.useCacheAnyway()) {
                return null;
            }
            JsonData data = JsonData.create(mRawData.getData());
            T ret = cacheAble.processRawDataFromCache(data);
            cacheAble.onCacheData(CacheResultType.FROM_INIT_FILE, ret, outOfDate);
            return ret;
        }

        if (DEBUG) {
            CLog.d(LOG_TAG, "key: %s, cache file not exist", cacheKey);
        }
        return null;
    }

    public DiskCacheProvider getDiskCacheProvider() {
        return mFileCache;
    }

    private class InnerCacheTask<T1> extends SimpleTask {

        private ICacheAble<T1> mCacheAble;

        private CacheMetaData mRawData;
        private T1 mResult;
        private byte mWorkType = 0;
        private byte mConvertFor = 0;
        private byte mCurrentStatus = 0;

        public InnerCacheTask(ICacheAble<T1> cacheAble) {
            mCacheAble = cacheAble;
        }

        void beginQuery() {

            String cacheKey = mCacheAble.getCacheKey();
            if (mCacheAble.cacheIsDisabled()) {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "key: %s, Cache is disabled, query from server", cacheKey);
                }
                mCacheAble.onNoCacheData(CacheManager.this);
                return;
            }

            // try to find in runtime cache
            mRawData = mMemoryCache.get(cacheKey);
            if (mRawData != null) {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "key: %s, exist in list", cacheKey);
                }
                beginConvertDataAsync(CONVERT_FOR_MEMORY);
                return;
            }

            // try read from cache data
            boolean hasFileCache = mFileCache.getDiskCache().has(cacheKey);
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
                CLog.d(LOG_TAG, "key: %s, cache file not exist", mCacheAble.getCacheKey());
            }
            mCacheAble.onNoCacheData(CacheManager.this);
        }

        @Override
        public void doInBackground() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "key: %s, doInBackground: mWorkType: %s", mCacheAble.getCacheKey(), mWorkType);
            }
            switch (mWorkType) {

                case DO_READ_FROM_FILE:
                    doQueryFromCacheFileInBackground();
                    setCurrentStatus(AFTER_READ_FROM_FILE);
                    break;

                case DO_READ_FROM_ASSERT:
                    doQueryFromAssertCacheFileInBackground();
                    setCurrentStatus(AFTER_READ_FROM_ASSERT);
                    break;

                case DO_CONVERT:
                    doConvertDataInBackground();
                    setCurrentStatus(AFTER_CONVERT);
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onFinish(boolean canceled) {
            switch (mCurrentStatus) {
                case AFTER_READ_FROM_FILE:
                    beginConvertDataAsync(CONVERT_FOR_FILE);
                    break;
                case AFTER_READ_FROM_ASSERT:
                    beginConvertDataAsync(CONVERT_FOR_ASSERT);
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
                CLog.d(LOG_TAG, "key: %s, beginQueryFromCacheFileAsync", mCacheAble.getCacheKey());
            }
            mWorkType = DO_READ_FROM_FILE;
            restart();
            SimpleExecutor.getInstance().execute(this);
        }

        private void beginQueryFromAssertCacheFileAsync() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "key: %s, beginQueryFromAssertCacheFileAsync", mCacheAble.getCacheKey());
            }
            mWorkType = DO_READ_FROM_ASSERT;
            restart();
            SimpleExecutor.getInstance().execute(this);
        }

        private void beginConvertDataAsync(byte convertFor) {
            if (DEBUG) {
                CLog.d(LOG_TAG, "key: %s, beginConvertDataAsync", mCacheAble.getCacheKey());
            }
            mConvertFor = convertFor;
            mWorkType = DO_CONVERT;
            restart();
            SimpleExecutor.getInstance().execute(this);
        }

        private void doQueryFromCacheFileInBackground() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "key: %s, try read cache data from file", mCacheAble.getCacheKey());
            }

            String cacheContent = mFileCache.read(mCacheAble.getCacheKey());
            JsonData jsonData = JsonData.create(cacheContent);
            mRawData = CacheMetaData.createFromJson(jsonData);
        }

        private void doQueryFromAssertCacheFileInBackground() {

            if (DEBUG) {
                CLog.d(LOG_TAG, "key: %s, try read cache data from assert file", mCacheAble.getCacheKey());
            }

            String cacheContent = DiskFileUtils.readAssert(mContext, mCacheAble.getAssertInitDataPath());
            mRawData = CacheMetaData.createInvalidated(cacheContent);
            putDataToMemoryCache(mCacheAble.getCacheKey(), mRawData);
        }

        private void doConvertDataInBackground() {
            if (DEBUG) {
                CLog.d(LOG_TAG, "key: %s, doConvertDataInBackground", mCacheAble.getCacheKey());
            }
            JsonData data = JsonData.create(mRawData.getData());
            mResult = mCacheAble.processRawDataFromCache(data);
        }

        private void setCurrentStatus(byte status) {
            mCurrentStatus = status;
            if (DEBUG) {
                CLog.d(LOG_TAG, "key: %s, setCurrentStatus: %s", mCacheAble.getCacheKey(), status);
            }
        }

        private void done() {

            boolean outOfDate = mRawData.isOutOfDateFor(mCacheAble);

            if (mResult != null) {
                switch (mConvertFor) {
                    case CONVERT_FOR_ASSERT:
                        mCacheAble.onCacheData(CacheResultType.FROM_INIT_FILE, mResult, outOfDate);
                        break;
                    case CONVERT_FOR_CREATE:
                        mCacheAble.onCacheData(CacheResultType.FROM_CREATED, mResult, outOfDate);
                        break;
                    case CONVERT_FOR_FILE:
                        mCacheAble.onCacheData(CacheResultType.FROM_INIT_FILE, mResult, outOfDate);
                        break;
                    case CONVERT_FOR_MEMORY:
                        mCacheAble.onCacheData(CacheResultType.FROM_CACHE_FILE, mResult, outOfDate);
                        break;
                }
            }
            if (mResult == null || outOfDate) {
                mCacheAble.onNoCacheData(CacheManager.this);
            }
        }
    }
}