package in.srain.cube.cache;

import android.content.Context;
import android.text.TextUtils;
import in.srain.cube.Cube;

public class CacheManagerFactory {

    private static CacheManager sDefault;

    private static final String DEFAULT_CACHE_DIR = "cube-default-cache";
    private static final int DEFAULT_CACHE_MEMORY_CACHE_SIZE = 1024;    // 1M
    private static final int DEFAULT_CACHE_DISK_CACHE_SIZE = 1024 * 10; // 10M

    public static CacheManager getDefault() {
        if (sDefault == null) {
            initDefaultCache(Cube.getInstance().getContext(), DEFAULT_CACHE_DIR, DEFAULT_CACHE_MEMORY_CACHE_SIZE, DEFAULT_CACHE_DISK_CACHE_SIZE);
        }
        return sDefault;
    }

    public static void initDefaultCache(Context content, String cacheDir, int memoryCacheSizeInKB, int fileCacheSizeInKB) {
        if (TextUtils.isEmpty(cacheDir)) {
            cacheDir = DEFAULT_CACHE_DIR;
        }
        if (memoryCacheSizeInKB <= 0) {
            memoryCacheSizeInKB = DEFAULT_CACHE_MEMORY_CACHE_SIZE;
        }

        if (fileCacheSizeInKB <= 0) {
            fileCacheSizeInKB = DEFAULT_CACHE_DISK_CACHE_SIZE;
        }
        sDefault = new CacheManager(content, cacheDir, memoryCacheSizeInKB, fileCacheSizeInKB);
    }
}