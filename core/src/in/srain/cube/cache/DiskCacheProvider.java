package in.srain.cube.cache;

import android.content.Context;
import in.srain.cube.concurrent.SimpleExecutor;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.diskcache.CacheEntry;
import in.srain.cube.diskcache.DiskCache;
import in.srain.cube.diskcache.lru.SimpleDiskLruCache;
import in.srain.cube.util.CLog;

import java.io.File;
import java.io.IOException;

/**
 * It's easy to use.
 * Auto open.
 */
public class DiskCacheProvider {

    public interface AsyncTaskEventHandler {
        public void onEvent(int type);
    }

    public static final boolean DEBUG = true;
    protected static final String LOG_TAG = "cube-disk-cache-provider";

    public static final byte TASK_INIT_CACHE = 1;
    public static final byte TASK_CLOSE_CACHE = 2;
    public static final byte TASK_FLUSH_CACHE = 3;

    protected DiskCache mDiskCache;
    private boolean mIsDelayFlushing = false;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private boolean mDiskCacheReady = false;

    private AsyncTaskEventHandler mAsyncTaskEventHandler;

    public void setAsyncTaskEventHandler(AsyncTaskEventHandler handler) {
        mAsyncTaskEventHandler = handler;
    }

    public DiskCacheProvider(DiskCache diskCache) {
        mDiskCache = diskCache;
    }

    public static DiskCacheProvider createLru(Context context, File path, long size) {
        SimpleDiskLruCache simpleDiskLruCache = new SimpleDiskLruCache(path, 1, size);
        DiskCacheProvider provider = new DiskCacheProvider(simpleDiskLruCache);
        return provider;
    }

    public void write(String key, String str) {
        if (key == null) {
            return;
        }
        try {
            CacheEntry cacheEntry = getDiskCache().beginEdit(key);
            cacheEntry.setString(str);
            cacheEntry.commit();
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
    }

    public String read(String fileCacheKey) {
        try {
            CacheEntry cacheEntry = getDiskCache().getEntry(fileCacheKey);
            if (cacheEntry != null) {
                return cacheEntry.getString();
            }
        } catch (Exception e) {
            if (DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * initiate the disk cache
     */
    public void openDiskCacheAsync() {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s: initDiskCacheAsync", mDiskCache);
        }
        synchronized (mDiskCacheLock) {
            mDiskCacheStarting = true;
            new FileCacheTask(TASK_INIT_CACHE).executeNow();
        }
    }

    /**
     * close the disk cache
     */
    public void closeDiskCacheAsync() {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s: closeDiskCacheAsync", mDiskCache);
        }
        new FileCacheTask(TASK_CLOSE_CACHE).executeNow();
    }

    /**
     * flush the data to disk cache
     */
    public void flushDiskCacheAsync() {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, flushDishCacheAsync", mDiskCache);
        }
        new FileCacheTask(TASK_FLUSH_CACHE).executeNow();
    }

    /**
     * flush the data to disk cache
     */
    public void flushDiskCacheAsyncWithDelay(int delay) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "%s, flushDiskCacheAsyncWithDelay", delay);
        }
        if (mIsDelayFlushing) {
            return;
        }
        mIsDelayFlushing = true;
        new FileCacheTask(TASK_FLUSH_CACHE).executeAfter(delay);
    }

    /**
     * If disk is not read, will prepare it first.
     *
     * @return
     */
    public DiskCache getDiskCache() {
        if (!mDiskCacheReady) {
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s, try to access disk cache, but it is not open, try to open it.", mDiskCache);
            }
            openDiskCacheAsync();
        }
        synchronized (mDiskCacheLock) {
            while (mDiskCacheStarting) {
                try {
                    if (DEBUG) {
                        CLog.d(LOG_TAG, "%s, try to access, but disk cache is not ready, wait", mDiskCache);
                    }
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return mDiskCache;
    }

    /**
     * A helper class to encapsulate the operate into a Work which will be executed by the Worker.
     */
    private class FileCacheTask extends SimpleTask {

        private byte mTaskType;

        private FileCacheTask(byte taskType) {
            mTaskType = taskType;
        }

        @Override
        public void doInBackground() {

            try {
                doWork();
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        private void doWork() throws IOException {
            switch (mTaskType) {
                case TASK_INIT_CACHE:

                    synchronized (mDiskCacheLock) {
                        if (DEBUG) {
                            CLog.d(LOG_TAG, "begin open disk cache: " + mDiskCache);
                        }
                        mDiskCache.open();
                        mDiskCacheReady = true;
                        mDiskCacheStarting = false;
                        if (DEBUG) {
                            CLog.d(LOG_TAG, "disk cache open successfully, notify all lock: " + mDiskCache);
                        }
                        mDiskCacheLock.notifyAll();
                    }

                    break;
                case TASK_CLOSE_CACHE:
                    mDiskCache.close();
                    break;
                case TASK_FLUSH_CACHE:
                    mDiskCache.flush();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onFinish(boolean canceled) {
            if (mAsyncTaskEventHandler != null) {
                mAsyncTaskEventHandler.onEvent(mTaskType);
            }
        }

        void executeNow() {
            SimpleExecutor.getInstance().execute(this);
        }

        void executeAfter(int delay) {
            postDelay(new Runnable() {
                @Override
                public void run() {
                    executeNow();
                }
            }, delay);
        }
    }
}
