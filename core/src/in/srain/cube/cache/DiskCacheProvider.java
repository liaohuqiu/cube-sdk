package in.srain.cube.cache;

import android.content.Context;
import android.util.Log;
import in.srain.cube.concurrent.SimpleExecutor;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.diskcache.CacheEntry;
import in.srain.cube.diskcache.DiskCache;
import in.srain.cube.diskcache.lru.SimpleDiskLruCache;

import java.io.File;
import java.io.IOException;

public class DiskCacheProvider {

    protected static final boolean DEBUG = true;
    protected static final String TAG = "cube-file-cache";

    protected static final byte TASK_INIT_CACHE = 1;
    protected static final byte TASK_CLOSE_CACHE = 2;
    protected static final byte TASK_FLUSH_CACHE = 3;
    protected DiskCache mDiskCache;
    private boolean mIsDelayFlushing = false;

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
            CacheEntry cacheEntry = mDiskCache.beginEdit(key);
            cacheEntry.setString(str);
            cacheEntry.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String read(String fileCacheKey) {
        try {
            CacheEntry cacheEntry = mDiskCache.getEntry(fileCacheKey);
            if (cacheEntry != null) {
                return cacheEntry.getString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * initiate the disk cache
     */
    public void openDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "initDiskCacheAsync " + this);
        }
        new FileCacheTask(TASK_INIT_CACHE).executeNow();
    }

    /**
     * close the disk cache
     */
    public void closeDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "closeDiskCacheAsync");
        }
        new FileCacheTask(TASK_CLOSE_CACHE).executeNow();
    }

    /**
     * flush the data to disk cache
     */
    public void flushDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "flushDishCacheAsync");
        }
        new FileCacheTask(TASK_FLUSH_CACHE).executeNow();
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
        new FileCacheTask(TASK_FLUSH_CACHE).executeAfter(delay);
    }

    public DiskCache getDiskCache() {
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
                e.printStackTrace();
            }
        }

        private void doWork() throws IOException {
            switch (mTaskType) {
                case TASK_INIT_CACHE:
                    mDiskCache.open();
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
