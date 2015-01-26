package in.srain.cube.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import in.srain.cube.diskcache.CacheEntry;
import in.srain.cube.diskcache.DiskCache;
import in.srain.cube.diskcache.lru.SimpleDiskLruCache;
import in.srain.cube.cache.DiskCacheProvider;
import in.srain.cube.image.impl.SimpleDownloader;
import in.srain.cube.util.CLog;
import in.srain.cube.util.Debug;

import java.io.*;

/**
 * This class handles disk and memory caching of bitmaps.
 * <p/>
 * Most of the code is taken from the Android best practice of displaying Bitmaps <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 *
 * @author http://www.liaohuqiu.net
 */
public class ImageDiskCacheProvider extends DiskCacheProvider {

    protected static final boolean DEBUG = Debug.DEBUG_IMAGE;
    protected static final String LOG_TAG = Debug.DEBUG_IMAGE_LOG_TAG_PROVIDER;

    // Compression settings when writing images to disk cache
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
    private static final int DEFAULT_COMPRESS_QUALITY = 70;

    public ImageDiskCacheProvider(DiskCache diskCache) {
        super(diskCache);
    }

    public static ImageDiskCacheProvider createLru(long size, File path) {
        SimpleDiskLruCache simpleDiskLruCache = new SimpleDiskLruCache(path, 1, size);
        ImageDiskCacheProvider provider = new ImageDiskCacheProvider(simpleDiskLruCache);
        return provider;
    }

    public FileInputStream getInputStream(String key) {
        if (!mDiskCache.has(key)) {
            return null;
        }
        try {
            CacheEntry cacheEntry = mDiskCache.getEntry(key);
            return (FileInputStream) cacheEntry.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public FileInputStream downloadAndGetInputStream(String fileCacheKey, String url) {
        try {
            CacheEntry cacheEntry = mDiskCache.beginEdit(fileCacheKey);
            if (cacheEntry != null) {
                OutputStream outputStream = cacheEntry.newOutputStream();
                boolean ret = SimpleDownloader.downloadUrlToStream(url, outputStream);
                if (DEBUG) {
                    CLog.i(LOG_TAG, "download: %s %s %s", ret, fileCacheKey, url);
                }
                if (ret) {
                    cacheEntry.commit();
                    InputStream inputStream = cacheEntry.getInputStream();
                    if (inputStream instanceof FileInputStream) {
                        return (FileInputStream) inputStream;
                    }
                } else {
                    cacheEntry.abortEdit();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

        // Add to disk cache
        OutputStream out = null;
        try {
            CacheEntry cacheEntry = mDiskCache.beginEdit(key);
            if (cacheEntry != null) {
                out = cacheEntry.newOutputStream();
                bitmap.compress(DEFAULT_COMPRESS_FORMAT, DEFAULT_COMPRESS_QUALITY, out);
                cacheEntry.commit();
                out.close();
            }
        } catch (final IOException e) {
            CLog.e(LOG_TAG, "addBitmapToCache - " + e);
        } catch (Exception e) {
            CLog.e(LOG_TAG, "addBitmapToCache - " + e);
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
