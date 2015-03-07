package in.srain.cube.image.impl;

import android.graphics.drawable.BitmapDrawable;
import android.support.v4.util.LruCache;
import android.util.Log;
import in.srain.cube.image.ImageProvider;
import in.srain.cube.image.drawable.RecyclingBitmapDrawable;
import in.srain.cube.image.iface.ImageMemoryCache;
import in.srain.cube.util.CubeDebug;

public class DefaultMemoryCache implements ImageMemoryCache {

    protected static final boolean DEBUG = CubeDebug.DEBUG_IMAGE;
    protected static final String LOG_TAG = CubeDebug.DEBUG_IMAGE_LOG_TAG_PROVIDER;
    private LruCache<String, BitmapDrawable> mMemoryCache;

    public DefaultMemoryCache(int cacheSizeInKB) {

        // Set up memory cache
        if (DEBUG) {
            Log.d(LOG_TAG, "Memory cache created (size = " + cacheSizeInKB + " KB)");
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
                final int bitmapSize = (int) (ImageProvider.getBitmapSize(value) / 1024);
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
                Log.d(LOG_TAG, "Memory cache cleared");
            }
        }
    }

    @Override
    public void delete(String key) {
        mMemoryCache.remove(key);
    }

    @Override
    public long getMaxSize() {
        return mMemoryCache.maxSize() * 1024;
    }

    @Override
    public long getUsedSpace() {
        return mMemoryCache.size() * 1024;
    }
}
