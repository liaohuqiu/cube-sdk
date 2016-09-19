package in.srain.cube.image;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import in.srain.cube.cache.DiskCacheProvider;
import in.srain.cube.diskcache.CacheEntry;
import in.srain.cube.diskcache.DiskCache;
import in.srain.cube.diskcache.lru.SimpleDiskLruCache;
import in.srain.cube.image.iface.ImageDownloader;
import in.srain.cube.image.impl.SimpleDownloader;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;

import java.io.*;

/**
 * This class handles disk and memory caching of bitmaps.
 * <p/>
 * Most of the code is taken from the Android best practice of displaying Bitmaps <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 *
 * @author http://www.liaohuqiu.net
 */
public class ImageDiskCacheProvider extends DiskCacheProvider {

    protected static final boolean DEBUG = CubeDebug.DEBUG_IMAGE;
    protected static final String LOG_TAG = CubeDebug.DEBUG_IMAGE_LOG_TAG_PROVIDER;

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

    public long getSize(String key) {
        if (!mDiskCache.has(key)) {
            return -1;
        }
        try {
            CacheEntry cacheEntry = mDiskCache.getEntry(key);
            return cacheEntry.getSize();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
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

    public FileInputStream downloadAndGetInputStream(ImageDownloader imageDownloader, ImageTask imageTask, String fileCacheKey, String url) {
        if (imageDownloader == null) {
            imageDownloader = SimpleDownloader.getInstance();
        }
        try {
            CacheEntry cacheEntry = mDiskCache.beginEdit(fileCacheKey);
            if (cacheEntry != null) {
                OutputStream outputStream = cacheEntry.newOutputStream();
                boolean ret = imageDownloader.downloadToStream(imageTask, url, outputStream, null);
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
            if (CubeDebug.DEBUG_IMAGE) {
                e.printStackTrace();
            }
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
