package net.liaohuqiu.cube.image.imple;

import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.util.Log;

import net.liaohuqiu.cube.image.ImageProvider;
import net.liaohuqiu.cube.image.drawable.RecyclingBitmapDrawable;
import net.liaohuqiu.cube.image.iface.ImageMemoryCache;
import net.liaohuqiu.cube.util.CLog;

public class DefaultMemoryCache implements ImageMemoryCache {

	protected static final boolean DEBUG = CLog.DEBUG_IMAGE;
	protected static final String TAG = "image_provider";
	private LruCache<String, BitmapDrawable> mMemoryCache;

	private static DefaultMemoryCache sDefault;

	public static DefaultMemoryCache getDefault() {
		if (null == sDefault) {
			int size = Math.round(0.2f * Runtime.getRuntime().maxMemory() / 1024);
			sDefault = new DefaultMemoryCache(size);
		}
		return sDefault;
	}

	public DefaultMemoryCache(int cacheSizeInKB) {

		// Set up memory cache
		if (DEBUG) {
			Log.d(TAG, "Memory cache created (size = " + cacheSizeInKB + " KB)");
		}

		mMemoryCache = new LruCache<String, BitmapDrawable>(cacheSizeInKB) {

			/**
			 * Notify the removed entry that is no longer being cached
			 */
			@Override
			protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
				if (RecyclingBitmapDrawable.class.isInstance(oldValue)) {
					// The removed entry is a recycling drawable, so notify it
					// that it has been removed from the memory cache
					((RecyclingBitmapDrawable) oldValue).setIsCached(false);
				} else {
					// The removed entry is a standard BitmapDrawable
					// do nothing
				}
			}

			/**
			 * Measure item size in kilobytes rather than units which is more practical for a bitmap cache
			 */
			@Override
			protected int sizeOf(String key, BitmapDrawable value) {
				final int bitmapSize = ImageProvider.getBitmapSize(value) / 1024;
				return bitmapSize == 0 ? 1 : bitmapSize;
			}
		};
	}

	@Override
	public void set(String key, BitmapDrawable drawable) {
		if (key == null || drawable == null) {
			return;
		}

		// Add to memory cache
		if (mMemoryCache != null) {
			if (RecyclingBitmapDrawable.class.isInstance(drawable)) {
				// The removed entry is a recycling drawable, so notify it
				// that it has been added into the memory cache
				((RecyclingBitmapDrawable) drawable).setIsCached(true);
			}
			mMemoryCache.put(key, drawable);
		}
	}

	/**
	 * Get from memory cache.
	 */
	@Override
	public BitmapDrawable get(String key) {
		BitmapDrawable memValue = null;
		if (mMemoryCache != null) {
			memValue = mMemoryCache.get(key);
		}
		return memValue;
	}

	/**
	 * clear the memory cache
	 */
	@Override
	public void clear() {
		if (mMemoryCache != null) {
			mMemoryCache.evictAll();
			if (DEBUG) {
				Log.d(TAG, "Memory cache cleared");
			}
		}
	}
}
