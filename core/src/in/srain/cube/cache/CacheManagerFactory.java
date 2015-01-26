package in.srain.cube.cache;

import android.content.Context;
import in.srain.cube.Cube;

public class CacheManagerFactory {

    private static CacheManager sDefault;

    private static final String DEFAULT_CACHE = "cube-default-cache";
    private static final int DEFAULT_CACHE_MEMORY_CACHE_SIZE = 1024;
    private static final int DEFAULT_CACHE_DISK_CACHE_SIZE = 1024 * 10;

    public static CacheManager getDefault() {
        if (sDefault == null) {
            initDefaultCache(Cube.getInstance().getContext(), DEFAULT_CACHE, DEFAULT_CACHE_MEMORY_CACHE_SIZE, DEFAULT_CACHE_DISK_CACHE_SIZE);
        }
        return sDefault;
    }

    public static void initDefaultCache(Context content, String cacheDir, int memoryCacheSizeInKB, int fileCacheSizeInKB) {
        sDefault = CacheManager.create(content, cacheDir, memoryCacheSizeInKB, fileCacheSizeInKB);
    }
}