package in.srain.cube.image.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import in.srain.cube.concurrent.SimpleExecutor;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.file.DiskLruCache;
import in.srain.cube.file.DiskLruCache.Editor;
import in.srain.cube.file.FileUtil;
import in.srain.cube.image.iface.ImageFileProvider;
import in.srain.cube.util.Debug;

import java.io.*;

/**
 * This class handles disk and memory caching of bitmaps.
 * <p/>
 * Most of the code is taken from the Android best practice of displaying Bitmaps <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 *
 * @author http://www.liaohuqiu.net
 */
public class LruImageFileProvider implements ImageFileProvider {

    protected static final boolean DEBUG = Debug.DEBUG_IMAGE;

    protected static final String TAG = "image_provider";

    private static final String DEFAULT_CACHE_DIR = "cube-image";
    private static final int DEFAULT_CACHE_SIZE = 1024 * 1024 * 10;
    private static LruImageFileProvider sDefault;

    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;
    private static final int DISK_CACHE_INDEX = 0;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private boolean mDiskCacheReady = false;
    private File mDiskCacheDir;
    private long mDiskCacheSize;

    private long mLastFlushTime = 0;

    @Override
    public FileInputStream getInputStream(String fileCacheKey) {
        return read(fileCacheKey);
    }

    @Override
    public FileInputStream downloadAndGetInputStream(String fileCacheKey, String url) {
        try {
            Editor editor = open(fileCacheKey);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(0);
                SimpleDownloader.downloadUrlToStream(url, outputStream);
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return read(fileCacheKey);
    }

    protected enum FileCacheTaskType {
        init_cache, close_cache, flush_cache
    }

    public LruImageFileProvider(long size, File path) {
        mDiskCacheSize = size;
        mDiskCacheDir = path;
    }

    public static LruImageFileProvider getDefault(Context context) {
        if (null == sDefault) {
            FileUtil.CacheDirInfo cacheDirInfo = FileUtil.getDiskCacheDir(context, DEFAULT_CACHE_DIR, DEFAULT_CACHE_SIZE);
            sDefault = new LruImageFileProvider(cacheDirInfo.realSize, cacheDirInfo.path);
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
                    if (FileUtil.getUsableSpace(mDiskCacheDir) > mDiskCacheSize) {
                        try {
                            mDiskLruCache = DiskLruCache.open(mDiskCacheDir, 1, 1, mDiskCacheSize);
                            if (DEBUG) {
                                Log.d(TAG, "Disk cache initialized " + this);
                            }
                        } catch (final IOException e) {
                            Log.e(TAG, "initDiskCache - " + e);
                        }
                    } else {
                        Log.e(TAG, String.format("no enough space for initDiskCache %s %s", FileUtil.getUsableSpace(mDiskCacheDir), mDiskCacheSize));
                    }
                }
            }
            mDiskCacheStarting = false;
            mDiskCacheReady = true;
            mDiskCacheLock.notifyAll();
        }
    }

    /**
     * Adds a bitmap to both memory and disk cache
     *
     * @param key    Unique identifier for the bitmap to store
     * @param bitmap The bitmap to store
     */
    public void write(String key, Bitmap bitmap) {
        if (key == null || bitmap == null) {
            return;
        }

        synchronized (mDiskCacheLock) {

            // Add to disk cache
            if (mDiskLruCache != null) {
                OutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot == null) {
                        final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            out = editor.newOutputStream(DISK_CACHE_INDEX);
                            bitmap.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, out);
                            editor.commit();
                            out.close();
                        }
                    }
                } catch (final IOException e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } catch (Exception e) {
                    Log.e(TAG, "addBitmapToCache - " + e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private FileInputStream read(String fileCacheKey) {
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
                InputStream inputStream = null;
                DiskLruCache.Snapshot snapshot = null;
                try {
                    snapshot = mDiskLruCache.get(fileCacheKey);

                } catch (final IOException e) {
                    Log.e(TAG, "getBitmapFromDiskCache - " + e);
                }

                if (snapshot == null) {
                    return null;
                } else {
                    inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                    return (FileInputStream) inputStream;
                }
            }
            return null;
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
            long now = System.currentTimeMillis();
            if (now - 1000 < mLastFlushTime) {
                return;
            }
            mLastFlushTime = now;
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
     *
     * @author http://www.liaohuqiu.net
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

        void excute() {
            SimpleExecutor.getInstance().execute(this);
        }
    }

    /**
     * initiate the disk cache
     */
    public void initDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "initDiskCacheAsync " + this);
        }
        new FileCacheTask(FileCacheTaskType.init_cache).excute();
    }

    /**
     * close the disk cache
     */
    public void closeDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "closeDiskCacheAsync");
        }
        new FileCacheTask(FileCacheTaskType.close_cache).excute();
    }

    /**
     * flush the data to disk cache
     */
    @Override
    public void flushDiskCacheAsync() {
        if (DEBUG) {
            Log.d(TAG, "flushDishCacheAsync");
        }
        new FileCacheTask(FileCacheTaskType.flush_cache).excute();
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

    @Override
    public boolean has(String key) {
        if (mDiskLruCache != null) {
            return read(key) != null;
        }
        return false;
    }
}
