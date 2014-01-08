package com.srain.cube.image.imple;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.srain.cube.concurrent.SimpleExcutor;
import com.srain.cube.concurrent.SimpleTask;
import com.srain.cube.file.DiskLruCache;
import com.srain.cube.file.DiskLruCache.Editor;
import com.srain.cube.image.iface.ImageFileCache;
import com.srain.cube.util.CLog;
import com.srain.cube.util.Version;

/**
 * 
 * This class handles disk and memory caching of bitmaps.
 * 
 * Most of the code is taken from the Android best pratice of displaying Bitmaps <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 * 
 * @author huqiu.lhq
 */
public class LruImageFileCache {

	protected static final boolean DEBUG = CLog.DEBUG;

	protected static final String TAG = "image_provider";

	private static final String DEFAULT_CACHE_DIR = "cube-image";
	private static final int DEFAULT_CACHE_SIZE = 1024 * 1024 * 10;
	private static LruImageFileCache sDefault;

	// Compression settings when writing images to disk cache
	private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	private static final int DEFAULT_COMPRESS_QUALITY = 70;
	private static final int DISK_CACHE_INDEX = 0;

	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private boolean mDiskCacheReady = false;
	private File mDiskCacheDir;
	private int mDiskCacheSize;

	protected enum FileCacheTaskType {
		init_cache, close_cache, flush_cache
	}

	/**
	 * Create a new ImageProvider object using the specified parameters. This should not be called directly by other classes, instead use {@link ImageFileCache#getInstance(FragmentManager, ImageCacheParams)} to fetch an ImageProvider instance.
	 * 
	 * @param cacheParams
	 *            The cache parameters to use to initialize the cache
	 */
	public LruImageFileCache(int sizeInKB, File path) {
		mDiskCacheSize = sizeInKB;
		mDiskCacheDir = path;
	}

	public static LruImageFileCache getDefault(Context context) {
		if (null == sDefault) {
			sDefault = new LruImageFileCache(DEFAULT_CACHE_SIZE, getDiskCacheDir(context, DEFAULT_CACHE_DIR));
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
					if (getUsableSpace(mDiskCacheDir) > mDiskCacheSize) {
						try {
							mDiskLruCache = DiskLruCache.open(mDiskCacheDir, 1, 1, mDiskCacheSize);
							if (DEBUG) {
								Log.d(TAG, "Disk cache initialized " + this);
							}
						} catch (final IOException e) {
							if (DEBUG) {
								Log.e(TAG, "initDiskCache - " + e);
							}
						}
					} else {
						if (DEBUG) {
							Log.e(TAG, String.format("no enough space for initDiskCache %s %s", getUsableSpace(mDiskCacheDir), mDiskCacheSize));
						}
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
	 * @param key
	 *            Unique identifier for the bitmap to store
	 * @param bitmap
	 *            The bitmap to store
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

	public InputStream read(String fileCacheKey) {
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
					return inputStream;
				}
			}
			return null;
		}
	}

	public Editor open(String key) throws IOException {
		if (null != mDiskLruCache) {
			return mDiskLruCache.edit(key);
		}
		return null;
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
	public void flushDishCache() {
		synchronized (mDiskCacheLock) {
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
	 * Get a usable cache directory (external if available, internal otherwise).
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            A unique directory name to append to the cache dir
	 * @return The cache dir
	 */
	public static File getDiskCacheDir(Context context, String uniqueName) {
		// Check if media is mounted or storage is built-in, if so, try and use external cache dir
		// otherwise use internal cache dir
		String cachePath = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			cachePath = getExternalCacheDir(context).getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * Get the external app cache directory.
	 * 
	 * @param context
	 *            The context to use
	 * @return The external cache dir : /storage/sdcard0/Android/data/com.srain.sdk/cache
	 */
	@TargetApi(VERSION_CODES.FROYO)
	public static File getExternalCacheDir(Context context) {
		if (Version.hasFroyo()) {
			File path = context.getExternalCacheDir();
			return path;
		}

		// Before Froyo we need to construct the external cache dir ourselves
		final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
		return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
	}

	/**
	 * Check how much usable space is available at a given path.
	 * 
	 * @param path
	 *            The path to check
	 * @return The space available in bytes
	 */
	@SuppressWarnings("deprecation")
	@TargetApi(VERSION_CODES.GINGERBREAD)
	public static long getUsableSpace(File path) {
		if (Version.hasGingerbread()) {
			return path.getUsableSpace();
		}
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	/**
	 * A helper class to encapsulate the operate into a Work which will be excuted by the Worker.
	 * 
	 * @author huqiu.lhq
	 * 
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
				flushDishCache();
				break;
			default:
				break;
			}
		}

		@Override
		public void onFinish() {
		}

		void excute() {
			SimpleExcutor.getInstance().execute(this);
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
	public void flushDishCacheAsync() {
		if (DEBUG) {
			Log.d(TAG, "flushDishCacheAsync");
		}
		new FileCacheTask(FileCacheTaskType.flush_cache).excute();
	}
}
