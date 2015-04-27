package in.srain.cube.image;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import in.srain.cube.image.drawable.RecyclingBitmapDrawable;
import in.srain.cube.image.iface.ImageMemoryCache;
import in.srain.cube.image.iface.ImageReSizer;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;
import in.srain.cube.util.Version;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class handles disk and memory caching of bitmaps.
 * <p/>
 * Most of the code is taken from the Android best practice of displaying Bitmaps <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 *
 * @author http://www.liaohuqiu.net
 */
public class ImageProvider {

    protected static final boolean DEBUG = CubeDebug.DEBUG_IMAGE;

    protected static final String TAG = CubeDebug.DEBUG_IMAGE_LOG_TAG_PROVIDER;

    private static final String MSG_FETCH_BEGIN = "%s fetchBitmapData";
    private static final String MSG_FETCH_BEGIN_IDENTITY_KEY = "%s identityKey: %s";
    private static final String MSG_FETCH_BEGIN_FILE_CACHE_KEY = "%s fileCacheKey: %s";
    private static final String MSG_FETCH_BEGIN_IDENTITY_URL = "%s identityUrl: %s";
    private static final String MSG_FETCH_BEGIN_ORIGIN_URL = "%s originUrl: %s";

    private static final String MSG_FETCH_TRY_REUSE = "%s Disk Cache not hit. Try to reuse";
    private static final String MSG_FETCH_HIT_DISK_CACHE = "%s Disk Cache hit";
    private static final String MSG_FETCH_REUSE_SUCCESS = "%s reuse size: %s";
    private static final String MSG_FETCH_REUSE_FAIL = "%s reuse fail: %s, %s";
    private static final String MSG_FETCH_DOWNLOAD = "%s downloading: %s";
    private static final String MSG_DECODE = "%s decode: %sx%s inSampleSize:%s";

    private ImageMemoryCache mMemoryCache;
    private ImageDiskCacheProvider mDiskCacheProvider;

    public ImageProvider(Context context, ImageMemoryCache memoryCache, ImageDiskCacheProvider fileProvider) {
        mMemoryCache = memoryCache;
        mDiskCacheProvider = fileProvider;
    }

    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     *
     * @param config The bitmap configuration.
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
    @TargetApi(19) // @TargetApi(VERSION_CODES.KITKAT)
    public static long getBitmapSize(BitmapDrawable value) {
        if (null == value) {
            return 0;
        }
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
                // which will recycle automatically
                drawable = new RecyclingBitmapDrawable(resources, bitmap);
            }
        }
        return drawable;
    }

    /**
     * Get from memory cache.
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

    public void cancelTask(ImageTask task) {
        mDiskCacheProvider.getDiskCache().abortEdit(task.getFileCacheKey());
    }

    /**
     * Get Bitmap. If not exist in file cache, will try to re-use the file cache of the other sizes.
     * <p/>
     * If no file cache can be used, download then save to file.
     */
    public Bitmap fetchBitmapData(ImageLoader imageLoader, ImageTask imageTask, ImageReSizer imageReSizer) {
        Bitmap bitmap = null;
        if (mDiskCacheProvider == null) {
            return null;
        }
        FileInputStream inputStream = null;

        String fileCacheKey = imageTask.getFileCacheKey();
        ImageReuseInfo reuseInfo = imageTask.getRequest().getImageReuseInfo();

        if (DEBUG) {
            Log.d(TAG, String.format(MSG_FETCH_BEGIN, imageTask));
            Log.d(TAG, String.format(MSG_FETCH_BEGIN_IDENTITY_KEY, imageTask, imageTask.getIdentityKey()));
            Log.d(TAG, String.format(MSG_FETCH_BEGIN_FILE_CACHE_KEY, imageTask, fileCacheKey));
            Log.d(TAG, String.format(MSG_FETCH_BEGIN_ORIGIN_URL, imageTask, imageTask.getOriginUrl()));
            Log.d(TAG, String.format(MSG_FETCH_BEGIN_IDENTITY_URL, imageTask, imageTask.getIdentityUrl()));
        }

        // read from file cache
        inputStream = mDiskCacheProvider.getInputStream(fileCacheKey);

        // try to reuse
        if (inputStream == null) {
            if (reuseInfo != null && reuseInfo.getReuseSizeList() != null && reuseInfo.getReuseSizeList().length > 0) {
                if (DEBUG) {
                    Log.d(TAG, String.format(MSG_FETCH_TRY_REUSE, imageTask));
                }

                final String[] sizeKeyList = reuseInfo.getReuseSizeList();
                for (int i = 0; i < sizeKeyList.length; i++) {
                    String size = sizeKeyList[i];
                    final String key = imageTask.generateFileCacheKeyForReuse(size);
                    inputStream = mDiskCacheProvider.getInputStream(key);

                    if (inputStream != null) {
                        if (DEBUG) {
                            Log.d(TAG, String.format(MSG_FETCH_REUSE_SUCCESS, imageTask, size));
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
                Log.d(TAG, String.format(MSG_FETCH_HIT_DISK_CACHE, imageTask));
            }
        }

        if (imageTask.getStatistics() != null) {
            imageTask.getStatistics().s2_afterCheckFileCache(inputStream != null);
        }

        // We've got nothing from file cache
        if (inputStream == null) {
            String url = imageReSizer.getRemoteUrl(imageTask);
            if (DEBUG) {
                Log.d(TAG, String.format(MSG_FETCH_DOWNLOAD, imageTask, url));
            }
            inputStream = mDiskCacheProvider.downloadAndGetInputStream(imageLoader.getImageDownloader(), imageTask, fileCacheKey, url);
            if (imageTask.getStatistics() != null) {
                imageTask.getStatistics().s3_afterDownload();
            }
            if (inputStream == null) {
                imageTask.setError(ImageTask.ERROR_NETWORK);
                CLog.e(TAG, "%s download fail: %s %s", imageTask, fileCacheKey, url);
            }
        }
        if (inputStream != null) {
            try {
                bitmap = decodeSampledBitmapFromDescriptor(inputStream.getFD(), imageTask, imageReSizer);
                if (bitmap == null) {
                    imageTask.setError(ImageTask.ERROR_BAD_FORMAT);
                    CLog.e(TAG, "%s decode bitmap fail, bad format. %s, %s", imageTask, fileCacheKey, imageReSizer.getRemoteUrl(imageTask));
                }
            } catch (IOException e) {
                CLog.e(TAG, "%s decode bitmap fail, may be out of memory. %s, %s", imageTask, fileCacheKey, imageReSizer.getRemoteUrl(imageTask));
                if (CubeDebug.DEBUG_IMAGE) {
                    e.printStackTrace();
                }
            }
        } else {
            CLog.e(TAG, "%s fetch bitmap fail. %s, %s", imageTask, fileCacheKey, imageReSizer.getRemoteUrl(imageTask));
        }
        if (imageTask != null && imageTask.getStatistics() != null) {
            imageTask.getStatistics().s4_afterDecode(mDiskCacheProvider.getSize(fileCacheKey));
        }
        return bitmap;
    }

    private Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, ImageTask imageTask, ImageReSizer imageReSizer) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        imageTask.setBitmapOriginSize(options.outWidth, options.outHeight);

        // Calculate inSampleSize
        options.inSampleSize = imageReSizer.getInSampleSize(imageTask);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        if (DEBUG) {
            Log.d(TAG, String.format(MSG_DECODE, imageTask, imageTask.getBitmapOriginSize().x, imageTask.getBitmapOriginSize().y, options.inSampleSize));
        }

        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        return bitmap;
    }

    private Bitmap decodeSampledBitmapFromInputStream(InputStream stream, ImageTask imageTask, ImageReSizer imageReSizer) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // try to decode height and width from InputStream
        BitmapFactory.decodeStream(stream, null, options);

        imageTask.setBitmapOriginSize(options.outWidth, options.outHeight);

        // Calculate inSampleSize
        options.inSampleSize = imageReSizer.getInSampleSize(imageTask);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        if (DEBUG) {
            Log.d(TAG, String.format(MSG_DECODE, imageTask, imageTask.getBitmapOriginSize().x, imageTask.getBitmapOriginSize().y, options.inSampleSize));
        }

        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);

        return bitmap;
    }

    public void flushFileCache() {
        if (null != mDiskCacheProvider) {
            mDiskCacheProvider.flushDiskCacheAsync();
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
     * clear the disk cache
     */
    public void clearDiskCache() {
        if (null != mDiskCacheProvider) {
            try {
                mDiskCacheProvider.getDiskCache().clear();
            } catch (IOException e) {
            }
        }
    }

    public long getMemoryCacheMaxSpace() {
        return mMemoryCache.getMaxSize();
    }

    public long getMemoryCacheUsedSpace() {
        return mMemoryCache.getUsedSpace();
    }

    /**
     * return the file cache path
     *
     * @return
     */
    public String getFileCachePath() {
        if (null != mDiskCacheProvider) {
            return mDiskCacheProvider.getDiskCache().getDirectory().getAbsolutePath();
        }
        return null;
    }

    /**
     * get the used space
     *
     * @return
     */
    public long getFileCacheUsedSpace() {
        return null != mDiskCacheProvider ? mDiskCacheProvider.getDiskCache().getSize() : 0;
    }

    public long getFileCacheMaxSpace() {
        if (null != mDiskCacheProvider) {
            return mDiskCacheProvider.getDiskCache().getCapacity();
        }
        return 0;
    }
}
