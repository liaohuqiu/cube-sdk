package com.srain.cube.image;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.util.SparseArray;

import com.srain.cube.file.DiskLruCache;
import com.srain.cube.image.iface.ImageMemoryCache;
import com.srain.cube.image.iface.ImageResizer;
import com.srain.cube.image.imple.DefaultMemoryCache;
import com.srain.cube.image.imple.LruImageFileCache;
import com.srain.cube.image.util.Downloader;
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
public class ImageProvider {

	protected static final boolean DEBUG = CLog.DEBUG;

	protected static final String TAG = "image_provider";

	private static final String MSG_FETCH_BEGIN = "%s fetchBitmapData, %s";
	private static final String MSG_FETCH_TRY_REUSE = "%s Disk Cache not hit. Try to reuse, %s";
	private static final String MSG_FETCH_HIT_DISK_CACHE = "%s Disk Cache hit %s";
	private static final String MSG_FETCH_REUSE_SUCC = "%s reuse size: %s";
	private static final String MSG_FETCH_REUSE_FAIL = "%s reuse fail: %s";
	private static final String MSG_FETCH_DOWNLOAD = "%s not found in cache, downloading: %s";
	private static final String MSG_DECODE = "%s decode: %sx%s inSampleSize:%s";

	private ImageMemoryCache mMemoryCache;
	private LruImageFileCache mFileCache;

	private static ImageProvider sDefault;

	public static ImageProvider getDefault(Context context) {
		if (null == sDefault) {
			sDefault = new ImageProvider(DefaultMemoryCache.getDefault(), LruImageFileCache.getDefault(context));
		}
		return sDefault;
	}

	public ImageProvider(ImageMemoryCache memoryCache, LruImageFileCache fileCache) {
		mMemoryCache = memoryCache;
		mFileCache = fileCache;
	}

	/**
	 * Create a BitmapDrawable which can be managed in ImageProvider
	 * 
	 * @param resources
	 * @param bitmap
	 * @return
	 */
	public BitmapDrawable createBitmapDrawable(Resources resources, Bitmap bitmap) {
		if (bitmap == null) {
			return null;
		}
		BitmapDrawable drawable = null;
		if (bitmap != null) {
			if (Version.hasHoneycomb()) {
				// Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
				drawable = new BitmapDrawable(resources, bitmap);
			} else {
				// Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
				// which will recycle automagically
				drawable = new RecyclingBitmapDrawable(resources, bitmap);
			}
		}
		return drawable;
	}

	/**
	 * Get from memory cache.
	 * 
	 * @param key
	 *            Unique identifier for which item to get
	 * @return The bitmap drawable if found in cache, null otherwise
	 */

	public BitmapDrawable getBitmapFromMemCache(ImageTask imageTask) {
		BitmapDrawable memValue = null;

		if (mMemoryCache != null) {
			memValue = mMemoryCache.get(imageTask.getIdentityKey());
		}

		return memValue;
	}

	public void addBitmapToMemCache(String key, BitmapDrawable drawable) {

		// If the API level is lower than 11, do not use memory cache
		if (key == null || drawable == null || !Version.hasHoneycomb()) {
			return;
		}

		// Add to memory cache
		if (mMemoryCache != null) {
			mMemoryCache.set(key, drawable);
		}
	}

	private static String pringSparseArray(SparseArray<?> array) {
		if (array.size() <= 0) {
			return "{}";
		}

		StringBuilder buffer = new StringBuilder(array.size() * 28);
		buffer.append('{');
		for (int i = 0; i < array.size(); i++) {
			if (i > 0) {
				buffer.append(", ");
			}
			int key = array.keyAt(i);
			buffer.append(key);
			buffer.append('=');
			Object value = array.valueAt(i);
			if (value != array) {
				buffer.append(value);
			} else {
				buffer.append("(this Map)");
			}
		}
		buffer.append('}');
		return buffer.toString();
	}

	/**
	 * Get Bitmap
	 */
	public Bitmap fetchBitmapData(ImageTask imageTask, ImageResizer imageResizer) {
		Bitmap bitmap = null;
		if (mFileCache != null) {
			InputStream inputStream = null;

			String cacheKey = null;
			cacheKey = imageTask.getFileCacheKey();
			if (DEBUG) {
				Log.d(TAG, String.format(MSG_FETCH_BEGIN, imageTask, cacheKey));
			}

			inputStream = mFileCache.read(cacheKey);

			// try to reuse
			if (inputStream == null) {
				if (DEBUG) {
					Log.d(TAG, String.format(MSG_FETCH_TRY_REUSE, imageTask, cacheKey));
				}
				SparseArray<String> reuseInfos = imageTask.getReuseCacheKeys();
				for (int i = 0; i < reuseInfos.size(); i++) {
					int size = reuseInfos.keyAt(i);
					final String key = reuseInfos.valueAt(i);

					inputStream = mFileCache.read(cacheKey);
					if (inputStream != null) {
						cacheKey = key;
						if (DEBUG) {
							Log.d(TAG, String.format(MSG_FETCH_REUSE_SUCC, imageTask, size));
						}
						break;
					}
				}

				if (inputStream == null) {
					if (DEBUG) {
						Log.d(TAG, String.format(MSG_FETCH_REUSE_FAIL, imageTask, pringSparseArray(reuseInfos)));
					}
				}
			} else {
				if (DEBUG) {
					Log.d(TAG, String.format(MSG_FETCH_HIT_DISK_CACHE, imageTask, cacheKey));
				}
			}
			try {
				if (inputStream == null) {
					if (DEBUG) {
						Log.d(TAG, String.format(MSG_FETCH_DOWNLOAD, imageTask, imageResizer.getResizedUrl(imageTask)));
					}
					DiskLruCache.Editor editor = mFileCache.open(cacheKey);
					if (editor != null) {
						if (Downloader.downloadUrlToStream(imageResizer.getResizedUrl(imageTask), editor.newOutputStream(0))) {
							editor.commit();
						} else {
							editor.abort();
						}
					} else {
						Log.e(TAG, imageTask + " open editor fail.");
					}
					inputStream = mFileCache.read(cacheKey);
				}
				if (inputStream != null) {
					FileDescriptor fd = ((FileInputStream) inputStream).getFD();
					bitmap = decodeSampledBitmapFromDescriptor(fd, imageTask, imageResizer);
				} else {
					Log.e(TAG, imageTask + " fetch bitmap fail.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	private Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, ImageTask imageTask, ImageResizer imageResizer) {

		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		imageTask.setOriginSize(options.outWidth, options.outHeight);

		// Calculate inSampleSize
		options.inSampleSize = imageResizer.getInSampleSize(imageTask);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		if (DEBUG) {
			Log.d(TAG, String.format(MSG_DECODE, imageTask, imageTask.getOriginSize().x, imageTask.getOriginSize().y, options.inSampleSize));
		}

		return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
	}

	public void flushFileCache() {
		if (null != mFileCache) {
			mFileCache.flushDishCacheAsync();
		}
	}

	/**
	 * clear the memory cache
	 */
	public void clearMemoryCache() {
		if (mMemoryCache != null) {
			mMemoryCache.clear();
		}
	}

	/**
	 * @param candidate
	 *            - Bitmap to check
	 * @param targetOptions
	 *            - Options that have the out* value populated
	 * @return true if <code>candidate</code> can be used for inBitmap re-use with <code>targetOptions</code>
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {

		if (!Version.hasKitKat()) {
			// On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
			return candidate.getWidth() == targetOptions.outWidth && candidate.getHeight() == targetOptions.outHeight && targetOptions.inSampleSize == 1;
		}

		// From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
		// is smaller than the reusable bitmap candidate allocation byte count.
		int width = targetOptions.outWidth / targetOptions.inSampleSize;
		int height = targetOptions.outHeight / targetOptions.inSampleSize;
		int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
		return byteCount <= candidate.getAllocationByteCount();
	}

	/**
	 * Return the byte usage per pixel of a bitmap based on its configuration.
	 * 
	 * @param config
	 *            The bitmap configuration.
	 * @return The byte usage per pixel.
	 */
	private static int getBytesPerPixel(Config config) {
		if (config == Config.ARGB_8888) {
			return 4;
		} else if (config == Config.RGB_565) {
			return 2;
		} else if (config == Config.ARGB_4444) {
			return 2;
		} else if (config == Config.ALPHA_8) {
			return 1;
		}
		return 1;
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
	 * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat) onward this returns the allocated memory size of the bitmap which can be larger than the actual bitmap data byte count (in the case it was re-used).
	 * 
	 * @param value
	 * @return size in bytes
	 */
	@TargetApi(VERSION_CODES.KITKAT)
	public static int getBitmapSize(BitmapDrawable value) {
		Bitmap bitmap = value.getBitmap();

		// From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
		// larger than bitmap byte count.
		if (Version.hasKitKat()) {
			return bitmap.getAllocationByteCount();
		}

		if (Version.hasHoneycombMR1()) {
			return bitmap.getByteCount();
		}

		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
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
}
