package in.srain.cube.image;

import android.content.Context;
import in.srain.cube.app.lifecycle.IComponentContainer;
import in.srain.cube.app.lifecycle.LifeCycleComponentManager;
import in.srain.cube.file.FileUtil;
import in.srain.cube.image.iface.*;
import in.srain.cube.image.impl.*;

/**
 * @author http://www.liaohuqiu.net
 */
public class ImageLoaderFactory {

    private static ImageProvider sMutableImageProvider;
    private static ImageProvider sStableImageProvider;

    private static ImageResizer sDefaultImageResizer;
    private static ImageTaskExecutor sDefaultImageTaskExecutor;
    private static ImageLoadHandler sDefaultImageLoadHandler;

    public static void init(Context context) {
        int mutableDiskCacheSizeInKB = 1024 * 10;
        int stableDiskCacheSizeInKB = 1024 * 10;
        float percent = 0.2f;
        int memoryCacheSizeInKB = Math.round(percent * Runtime.getRuntime().maxMemory() / 1024);
        init(context, mutableDiskCacheSizeInKB, stableDiskCacheSizeInKB, memoryCacheSizeInKB);
    }

    public static void init(Context context, int mutableDiskCacheSizeInKB, int stableDiskCacheSizeInKB, int memoryCacheSizeInKB) {

        FileUtil.CacheDirInfo dirInfo1 = FileUtil.getDiskCacheDir(context, "mutable_image", mutableDiskCacheSizeInKB * 1024);
        FileUtil.CacheDirInfo dirInfo2 = FileUtil.getDiskCacheDir(context, "stable_image", stableDiskCacheSizeInKB * 1024);

        ImageFileProvider mutableImageFileProvider = new LruImageFileProvider(dirInfo1.realSize, dirInfo1.path);
        ImageFileProvider stableImageFileProvider = new LruImageFileProvider(dirInfo2.realSize, dirInfo2.path);

        ImageMemoryCache defaultMemoryCache = new DefaultMemoryCache(memoryCacheSizeInKB);

        mutableImageFileProvider.initDiskCacheAsync();
        stableImageFileProvider.initDiskCacheAsync();

        sMutableImageProvider = new ImageProvider(context, defaultMemoryCache, mutableImageFileProvider);
        sStableImageProvider = new ImageProvider(context, defaultMemoryCache, stableImageFileProvider);
    }

    public static ImageLoader createStableImageLoader(Context context) {
        return createStableImageLoader(context, sDefaultImageLoadHandler);
    }

    public static ImageLoader createStableImageLoader(Context context, ImageLoadHandler imageLoadHandler) {
        return createInner(context, sStableImageProvider, imageLoadHandler);
    }

    public static ImageLoader create(Context context) {
        return createMutableImageLoader(context, sDefaultImageLoadHandler);
    }

    public static ImageLoader create(Context context, ImageLoadHandler imageLoadHandler) {
        return createMutableImageLoader(context, imageLoadHandler);
    }

    public static ImageLoader createMutableImageLoader(Context context) {
        return createMutableImageLoader(context, sDefaultImageLoadHandler);
    }

    public static ImageLoader createMutableImageLoader(Context context, ImageLoadHandler imageLoadHandler) {
        return createInner(context, sMutableImageProvider, imageLoadHandler);
    }

    private static ImageLoader createInner(Context context, ImageProvider imageProvider, ImageLoadHandler imageLoadHandler) {
        return create(context, imageProvider, sDefaultImageTaskExecutor, sDefaultImageResizer, imageLoadHandler);
    }

    private static ImageLoader create(Context context, ImageProvider imageProvider, ImageTaskExecutor imageTaskExecutor, ImageResizer imageResizer, ImageLoadHandler imageLoadHandler) {

        if (imageProvider == null) {
            imageProvider = ImageProvider.getDefault(context);
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
        if (context instanceof IComponentContainer) {
            LifeCycleComponentManager.tryAddComponentToContainer(imageLoader, context);
        }
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
        sMutableImageProvider = imageProvider;
    }

    public static void setMutableImageProvider(ImageProvider imageProvider) {
        sMutableImageProvider = imageProvider;
    }

    public static void setStableImageProvider(ImageProvider imageProvider) {
        sMutableImageProvider = imageProvider;
    }
}
