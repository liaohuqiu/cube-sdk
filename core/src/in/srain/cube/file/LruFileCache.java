package in.srain.cube.file;

import android.content.Context;
import android.util.Log;
import in.srain.cube.cache.IFileCache;
import in.srain.cube.concurrent.SimpleExecutor;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.file.DiskLruCache.Editor;

import java.io.File;
import java.io.IOException;

public class LruFileCache implements IFileCache {

    protected static final boolean DEBUG = true;

    protected static final String TAG = "cube-file-cache";

    private static final String DEFAULT_CACHE_DIR = "cube-cache";

    private static final int DEFAULT_CACHE_SIZE = 1024 * 1024 * 10;

    private static LruFileCache sDefault;
    private static final int DISK_CACHE_INDEX = 0;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private boolean mDiskCacheReady = false;
    private File mDiskCacheDir;
    private long mDiskCacheSize;

    private boolean mIsDelayFlushing = false;

    protected enum FileCacheTaskType {
        init_cache, close_cache, flush_cache
    }

    public LruFileCache(Context context, String path, long size) {
        mDiskCacheSize = size;
        FileUtil.CacheDirInfo cacheDirInfo = FileUtil.getDiskCacheDir(context, path, size);
        mDiskCacheDir = cacheDirInfo.path;
        mDiskCacheSize = cacheDirInfo.realSize;
        if (cacheDirInfo.isNotEnough) {
            Log.e(TAG, String.format("no enough space for initDiskCache %s %s", cacheDirInfo.requireSize, cacheDirInfo.realSize));
        }
    }

    public static LruFileCache getDefault(Context context) {
        if (null == sDefault) {
            sDefault = new LruFileCache(context, DEFAULT_CACHE_DIR, DEFAULT_CACHE_SIZE);
            sDefault.initDiskCacheAsync();
        }
        return sDefault;
    }

    /**
     * Initializes the disk cache. Note that this includes disk access so this should not be executed on the main/UI thread. By default an ImageProvider does not initialize the disk cache when it is created, instead you should call initDiskCache() to initialize it on a background thread.
     */
    public void initDiskCache() {
        if (DEBUG) {
            Log.d(TAG, "initDiskCache " + this);
        }
        // Set up disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                if (mDiskCacheDir != null) {
                    if (!mDiskCacheDir.exists()) {
                        mDiskCacheDir.mkdirs();
                    }
                    try {
                        mDiskLruCache = DiskLruCache.open(mDiskCacheDir, 1, 1, mDiskCacheSize);
                        if (DEBUG) {
                            Log.d(TAG, "Disk cache initialized " + this);
                        }
                    } catch (final IOException e) {
                        Log.e(TAG, "initDiskCache - " + e);
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheReady = true;
            mDiskCacheLock.notifyAll();
        }
    }

    public void write(String key, String str) {
        if (key == null) {
            return;
        }

        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    final Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        editor.set(DISK_CACHE_INDEX, str);
                        editor.commit();
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String read(String fileCacheKey) {
        if (!mDiskCacheReady) {
            initDiskCache();
        }

        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    if (DEBUG) {
                        Log.d(TAG, "read wait " + this);
                    }
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                DiskLruCache.Snapshot snapshot = null;
                try {
                    snapshot = mDiskLruCache.get(fileCacheKey);

                } catch (final IOException e) {
                    e.printStackTrace();
                }

                if (snapshot == null) {
                    return null;
                }
                String str = null;
                try {
                    str = snapshot.getString(DISK_CACHE_INDEX);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return str;
            }
            return null;
        }
    }

    public boolean has(String key) {
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    if (DEBUG) {
                        Log.d(TAG, "check has wait " + this);
                    }
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        return false;
                    }
                    return snapshot.has(DISK_CACHE_INDEX);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void delete(String key) {
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    if (DEBUG) {
                        Log.d(TAG, "delete wait " + this);
                    }
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            try {
                mDiskLruCache.remove(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Editor open(String key) throws IOException {
        if (null != mDiskLruCache) {
            return mDiskLruCache.edit(key);
        } else {
            Log.e(TAG, "mDiskLruCache is null");
            return null;
        }
    }

    /**
     * Clears both the memory and disk cache associated with this ImageProvider object. Note that this includes disk access so this should not be executed on the main/UI thread.
     */
    public void clearCache() {

        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            mDiskCacheReady = false;

            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                    if (DEBUG) {
                        Log.d(TAG, "Disk cache cleared");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "clearCache - " + e);
                }
                mDiskLruCache = null;

                initDiskCache();
            }
        }
    }

    /**
     * Flushes the disk cache associated with this ImageProvider object. Note that this includes disk access so this should not be executed on the main/UI thread.
     */
    public void flushDiskCache() {
        synchronized (mDiskCacheLock) {
            mIsDelayFlushing = false;
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                    if (DEBUG) {
                        Log.d(TAG, "Disk cache flushed");
                    }
                } catch (IOException e) {
                    Log.e(TAG, "flush - " + e);
                }
            }
        }
    }

    /**
     * Closes the disk cache associated with this ImageProvider object. Note that this includes disk access so this should not be executed on the main/UI thread.
     */
    public void closeDiskCache() {
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                        if (DEBUG) {
                            Log.d(TAG, "Disk cache closed");
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "close - " + e);
                }
            }
        }
    }

    /**
     * A helper class to encapsulate the operate into a Work which will be executed by the Worker.
     */
    private class FileCacheTask extends SimpleTask {

        private FileCacheTask(FileCacheTaskType taskType) {
            mTaskType = taskType;
        }

        private FileCacheTaskType mTaskType;

        @Override
        public void doInBackground() {

            switch (mTaskType) {
                case init_cache:
                    initDiskCache();
                    break;
                case close_cache:
                    closeDiskCache();
                    break;
                case flush_cache:
                    flushDiskCache();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onFinish() {
        }

        void execute() {
            SimpleExecutor.getInstance().execute(this);
        }

        void execute(int delay) {
            SimpleTask.postDelay(new Runnable() {
                @Override
                public void run() {
                    execute();
                }
            }, delay);
        }
    }

    /**
     * initiate the disk cache
     */
    public void initDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "initDiskCacheAsync " + this);
        }
        new FileCacheTask(FileCacheTaskType.init_cache).execute();
    }

    /**
     * close the disk cache
     */
    public void closeDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "closeDiskCacheAsync");
        }
        new FileCacheTask(FileCacheTaskType.close_cache).execute();
    }

    /**
     * flush the data to disk cache
     */
    public void flushDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "flushDishCacheAsync");
        }
        new FileCacheTask(FileCacheTaskType.flush_cache).execute();
    }

    /**
     * flush the data to disk cache
     */
    public void flushDiskCacheAsyncWithDelay(int delay) {
        if (DEBUG) {
            Log.d(TAG, "flushDishCacheAsync");
        }
        if (mIsDelayFlushing) {
            return;
        }
        mIsDelayFlushing = true;
        new FileCacheTask(FileCacheTaskType.flush_cache).execute(delay);
    }

    @Override
    public String getCachePath() {
        return mDiskCacheDir.getPath();
    }

    @Override
    public long getUsedSpace() {
        if (null == mDiskLruCache) {
            return 0;
        }
        return mDiskLruCache.size();
    }

    @Override
    public long getMaxSize() {
        return mDiskCacheSize;
    }
}
