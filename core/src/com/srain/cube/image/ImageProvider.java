package com.srain.cube.image;

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
import android.text.TextUtils;
import android.util.Log;

import com.srain.cube.file.DiskLruCache;
import com.srain.cube.image.drawable.RecyclingBitmapDrawable;
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
 * Most of the code is taken from the Android best practice of displaying Bitmaps <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 * 
 * @author huqiu.lhq
 */
public class ImageProvider {

	protected static final boolean DEBUG = CLog.DEBUG_IMAGE;

	protected static final String TAG = "image_provider";

	private static final String MSG_FETCH_BEGIN = "%s fetchBitmapData, %s, size:%s";
	private static final String MSG_FETCH_TRY_REUSE = "%s Disk Cache not hit. Try to reuse, %s";
	private static final String MSG_FETCH_HIT_DISK_CACHE = "%s Disk Cache hit %s";
	private static final String MSG_FETCH_REUSE_SUCC = "%s reuse size: %s";
	private static final String MSG_FETCH_REUSE_FAIL = "%s reuse fail: %s, %s";
	private static final String MSG_FETCH_DOWNLOAD = "%s not found in cache, downloading: %s";
	private static final String MSG_DECODE = "%s decode: %sx%s inSampleSize:%s";

	private ImageMemoryCache mMemoryCache;
	private LruImageFileCache mFileCache;

	private static ImageProvider sDefault;

	public static ImageProvider getDefault(Context context) {
		if (null == sDefault) {
			sDefault = new ImageProvider(context, DefaultMemoryCache.getDefault(), LruImageFileCache.getDefault(context));
		}
		return sDefault;
	}

	public ImageProvider(Context context, ImageMemoryCache memoryCache, LruImageFileCache fileCache) {
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

	/**
	 * Get Bitmap
	 */
	public Bitmap fetchBitmapData(ImageTask imageTask, ImageResizer imageResizer) {
		Bitmap bitmap = null;
		if (mFileCache != null) {
			InputStream inputStream = null;

			String cacheKey = null;
			String indentitySizeKey = null;

			ImageReuseInfo reuseInfo = imageTask.getImageReuseInfo();
			if (reuseInfo != null) {
				indentitySizeKey = reuseInfo.getIndentitySize();
			}

			cacheKey = imageTask.genFileCacheKey(indentitySizeKey);
			if (DEBUG) {
				Log.d(TAG, String.format(MSG_FETCH_BEGIN, imageTask, cacheKey, indentitySizeKey));
			}

			inputStream = mFileCache.read(cacheKey);

			// try to reuse
			if (inputStream == null && reuseInfo != null && reuseInfo.getResuzeSize() != null) {
				if (DEBUG) {
					Log.d(TAG, String.format(MSG_FETCH_TRY_REUSE, imageTask, cacheKey));
				}

				final String[] sizeKeyList = reuseInfo.getResuzeSize();

				boolean canBeReused = false;
				for (int i = 0; i < sizeKeyList.length; i++) {
					String size = sizeKeyList[i];

					if (indentitySizeKey.equals(size)) {
						canBeReused = true;
						continue;
					}

					if (!TextUtils.isEmpty(size) && canBeReused) {
						final String key = imageTask.genFileCacheKey(size);
						inputStream = mFileCache.read(key);
						if (inputStream != null) {
							cacheKey = key;
							if (DEBUG) {
								Log.d(TAG, String.format(MSG_FETCH_REUSE_SUCC, imageTask, size));
							}
							break;
						} else {
							if (DEBUG) {
								Log.d(TAG, String.format(MSG_FETCH_REUSE_FAIL, imageTask, size, key));
							}
						}
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
					// bitmap = convertForImageViewScaleType(bitmap, imageTask);

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

		Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

		return bitmap;
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
}
