package in.srain.cube.image;

import android.content.Context;
import android.text.TextUtils;
import in.srain.cube.file.FileUtil;
import in.srain.cube.image.iface.*;
import in.srain.cube.image.impl.*;

import java.io.File;

/**
 * Create an {@link ImageLoader}.
 *
 * @author http://www.liaohuqiu.net
 */
public class ImageLoaderFactory {

    public static int DEFAULT_FILE_CACHE_SIZE_IN_KB = 10 * 1024; // 10M;
    private static String DEFAULT_FILE_CACHE_DIR = "cube-image";
    private static String STABLE_FILE_CACHE_DIR = "cube-image-stable";

    private static ImageProvider sDefaultImageProvider;
    private static ImageProvider sStableImageProvider;

    private static ImageResizer sDefaultImageResizer;
    private static ImageTaskExecutor sDefaultImageTaskExecutor;
    private static ImageLoadHandler sDefaultImageLoadHandler;
    private static ImageMemoryCache sDefaultImageMemoryCache;

    public static int getDefaultMemoryCacheSizeInKB() {
        float percent = 0.2f;
        int memoryCacheSizeInKB = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        return memoryCacheSizeInKB;
    }

    public static void customizeCache(Context context, int memoryCacheSizeInKB, int defaultDiskCacheSizeInKB) {
        customizeCache(context, memoryCacheSizeInKB, defaultDiskCacheSizeInKB, 0);
    }

    public static void customizeCache(Context context, int memoryCacheSizeInKB, String defaultDiskCachePath, int defaultDiskCacheSizeInKB) {
        customizeCache(context, memoryCacheSizeInKB, defaultDiskCachePath, defaultDiskCacheSizeInKB, null, 0);
    }

    public static void customizeCache(Context context, int memoryCacheSizeInKB, int defaultDiskCacheSizeInKB, int stableDiskCacheSizeInKB) {
        customizeCache(context, memoryCacheSizeInKB, null, defaultDiskCacheSizeInKB, null, stableDiskCacheSizeInKB);
    }

    /**
     * customize cache
     *
     * @param context
     * @param memoryCacheSizeInKB      How many memory should use. Will not be greater than 50% of free memory
     * @param defaultDiskCachePath     Default image cache path.
     *                                 If not specified, in {@link #DEFAULT_FILE_CACHE_DIR} under cache directory. External first.
     * @param defaultDiskCacheSizeInKB
     * @param stableDiskCachePath
     * @param stableDiskCacheSizeInKB
     */
    public static void customizeCache(Context context, int memoryCacheSizeInKB,
                                      String defaultDiskCachePath, int defaultDiskCacheSizeInKB,
                                      String stableDiskCachePath, int stableDiskCacheSizeInKB) {

        // init memory cache first
        if (memoryCacheSizeInKB > 0) {
            int maxCacheSizeInKB = Math.round(0.5f * Runtime.getRuntime().maxMemory() / 1024);
            memoryCacheSizeInKB = Math.max(memoryCacheSizeInKB, maxCacheSizeInKB);
            sDefaultImageMemoryCache = new DefaultMemoryCache(memoryCacheSizeInKB);
        }

        if (defaultDiskCacheSizeInKB > 0 && !TextUtils.isEmpty(defaultDiskCachePath)) {
            ImageFileProvider imageFileProvider = getImageFileProvider(context, defaultDiskCachePath, defaultDiskCacheSizeInKB, DEFAULT_FILE_CACHE_DIR);
            if (imageFileProvider != null) {
                sDefaultImageProvider = new ImageProvider(context, getDefaultImageMemoryCache(), imageFileProvider);
            }
        }

        if (stableDiskCacheSizeInKB > 0 && !TextUtils.isEmpty(stableDiskCachePath)) {
            ImageFileProvider imageFileProvider = getImageFileProvider(context, stableDiskCachePath, stableDiskCacheSizeInKB, STABLE_FILE_CACHE_DIR);
            if (imageFileProvider != null) {
                sStableImageProvider = new ImageProvider(context, getDefaultImageMemoryCache(), imageFileProvider);
            }
        }
    }

    private static ImageFileProvider getImageFileProvider(Context context, String path, int sizeInKB, String fallbackCachePath) {

        long size = (long) sizeInKB * 1024;

        ImageFileProvider imageFileProvider = null;
        if (!TextUtils.isEmpty(path)) {
            File cachePath = new File(path);
            // is not exist, try to make parent directory
            if (cachePath.exists() || cachePath.mkdirs()) {
                long free = FileUtil.getUsableSpace(cachePath);
                size = Math.min(size, free);
                imageFileProvider = new LruImageFileProvider(size, cachePath);
            }
        }

        if (imageFileProvider == null) {
            size = DEFAULT_FILE_CACHE_SIZE_IN_KB * 1024;
            FileUtil.CacheDirInfo dirInfo = FileUtil.getDiskCacheDir(context, fallbackCachePath, size);
            imageFileProvider = new LruImageFileProvider(dirInfo.realSize, dirInfo.path);
        }

        if (imageFileProvider != null) {
            imageFileProvider.initDiskCacheAsync();
        }
        return imageFileProvider;
    }

    public static ImageLoader createStableImageLoader(Context context) {
        return createInner(context, getStableImageProvider(context), sDefaultImageLoadHandler);
    }

    public static ImageLoader createStableImageLoader(Context context, ImageLoadHandler imageLoadHandler) {
        return createInner(context, getStableImageProvider(context), imageLoadHandler);
    }

    public static ImageLoader create(Context context) {
        return createInner(context, getDefaultImageProvider(context), sDefaultImageLoadHandler);
    }

    public static ImageLoader create(Context context, ImageLoadHandler imageLoadHandler) {
        return createInner(context, getDefaultImageProvider(context), imageLoadHandler);
    }

    private static ImageLoader createInner(Context context, ImageProvider imageProvider, ImageLoadHandler imageLoadHandler) {
        return create(context, imageProvider, sDefaultImageTaskExecutor, sDefaultImageResizer, imageLoadHandler);
    }

    private static ImageLoader create(Context context, ImageProvider imageProvider, ImageTaskExecutor imageTaskExecutor, ImageResizer imageResizer, ImageLoadHandler imageLoadHandler) {

        if (imageProvider == null) {
            imageProvider = getDefaultImageProvider(context);
        }

        if (imageTaskExecutor == null) {
            imageTaskExecutor = DefaultImageTaskExecutor.getInstance();
        }

        if (imageResizer == null) {
            imageResizer = DefaultImageResizer.getInstance();
        }

        if (imageLoadHandler == null) {
            imageLoadHandler = new DefaultImageLoadHandler(context);
        }
        ImageLoader imageLoader = new ImageLoader(context, imageProvider, imageTaskExecutor, imageResizer, imageLoadHandler);
        return imageLoader;
    }

    public static void setDefaultImageResizer(ImageResizer imageResizer) {
        sDefaultImageResizer = imageResizer;
    }

    public static void setDefaultImageTaskExecutor(ImageTaskExecutor imageTaskExecutor) {
        sDefaultImageTaskExecutor = imageTaskExecutor;
    }

    public static void setDefaultImageLoadHandler(ImageLoadHandler imageLoadHandler) {
        sDefaultImageLoadHandler = imageLoadHandler;
    }

    public static void setDefaultImageProvider(ImageProvider imageProvider) {
        sDefaultImageProvider = imageProvider;
    }

    public static void setStableImageProvider(ImageProvider imageProvider) {
        sStableImageProvider = imageProvider;
    }

    private static ImageProvider getDefaultImageProvider(Context context) {
        if (null == sDefaultImageProvider) {
            ImageFileProvider imageFileProvider = getImageFileProvider(context, null, 0, DEFAULT_FILE_CACHE_DIR);
            sDefaultImageProvider = new ImageProvider(context, getDefaultImageMemoryCache(), imageFileProvider);
        }
        return sDefaultImageProvider;
    }

    private static ImageProvider getStableImageProvider(Context context) {
        if (null == sStableImageProvider) {
            ImageFileProvider imageFileProvider = getImageFileProvider(context, null, 0, STABLE_FILE_CACHE_DIR);
            sStableImageProvider = new ImageProvider(context, getDefaultImageMemoryCache(), imageFileProvider);
        }
        return sStableImageProvider;
    }

    private static ImageMemoryCache getDefaultImageMemoryCache() {
        if (sDefaultImageMemoryCache == null) {
            sDefaultImageMemoryCache = new DefaultMemoryCache(getDefaultMemoryCacheSizeInKB());
        }
        return sDefaultImageMemoryCache;
    }
}
