package in.srain.cube.cache;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import in.srain.cube.util.CLog;
import in.srain.cube.util.Version;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;

public class DiskFileUtils {

    /**
     * @param context
     * @param absolutePath         if it's not absolutePath, will be path under cache dir
     * @param sizeInKB
     * @param fallbackRelativePath
     * @return
     */
    public static CacheDirInfo getDiskCacheDir(
            Context context, String absolutePath, int sizeInKB,
            String fallbackRelativePath) {

        long size = (long) sizeInKB * 1024;
        boolean done = false;

        CacheDirInfo dirInfo = new CacheDirInfo();
        dirInfo.requireSize = size;

        // treat as absolute path
        if (!TextUtils.isEmpty(absolutePath)) {
            File cachePath = new File(absolutePath);
            // is not exist, try to make parent directory
            if (cachePath.exists() || cachePath.mkdirs()) {
                long free = getUsableSpace(cachePath);
                size = Math.min(size, free);
                dirInfo.realSize = size;
                dirInfo.path = cachePath;
                done = true;
            }
        }

        // it's relative path
        if (!done) {
            if (TextUtils.isEmpty(fallbackRelativePath)) {
                fallbackRelativePath = absolutePath;
            }
            dirInfo = getDiskCacheDir(context, fallbackRelativePath, size);
        }
        return dirInfo;
    }

    /**
     * Get a usable cache directory (external if available, internal otherwise).
     * .
     * Check if media is mounted or storage is built-in, if so, try and use external cache folder
     * otherwise use internal cache folder
     * .
     * If both of them can not meet the requirement, use the bigger one.
     *
     * @param context    The context to use
     * @param uniqueName A unique folder name to append to the cache folder
     * @return The cache folder
     */
    public static CacheDirInfo getDiskCacheDir(Context context, String uniqueName, long requireSpace) {
        File sdPath = null;
        File internalPath = null;
        Long sdCardFree = 0L;

        boolean usingInternal = false;

        if (hasSDCardMounted()) {
            sdPath = getExternalCacheDir(context);
            if (!sdPath.exists()) {
                sdPath.mkdirs();
            }
            sdCardFree = getUsableSpace(sdPath);
        }

        CacheDirInfo cacheDirInfo = new CacheDirInfo();
        cacheDirInfo.requireSize = requireSpace;

        // sd card can not meet the requirement
        // try to use the build-in storage
        if (sdPath == null || sdCardFree < requireSpace) {
            internalPath = context.getCacheDir();
            long internalFree = getUsableSpace(internalPath);

            // both lower then requirement, choose the bigger one
            if (internalFree < requireSpace) {
                if (internalFree > sdCardFree) {
                    usingInternal = true;
                    cacheDirInfo.realSize = internalFree;
                } else {
                    usingInternal = false;
                    cacheDirInfo.realSize = sdCardFree;
                }
                cacheDirInfo.isNotEnough = true;
            } else {
                usingInternal = true;
                cacheDirInfo.realSize = requireSpace;
            }
        } else {
            usingInternal = false;
            cacheDirInfo.realSize = requireSpace;
        }

        cacheDirInfo.isInternal = usingInternal;
        if (usingInternal) {
            cacheDirInfo.path = new File(internalPath.getPath() + File.separator + uniqueName);
        } else {
            cacheDirInfo.path = new File(sdPath.getPath() + File.separator + uniqueName);
        }
        if (!cacheDirInfo.path.exists() && !cacheDirInfo.path.mkdirs()) {
            CLog.e("cube-cache", "can not create directory for: %s", cacheDirInfo.path);
        }
        return cacheDirInfo;
    }

    /**
     * Get the external application cache directory.
     *
     * @param context The context to use
     * @return The external cache folder : /storage/sdcard0/Android/data/com.srain.sdk/cache
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (Version.hasFroyo()) {
            File path = context.getExternalCacheDir();

            // In some case, even the sd card is mounted, getExternalCacheDir will return null, may be it is nearly full.
            if (path != null) {
                return path;
            }
        }

        // Before Froyo or the path is null, we need to construct the external cache folder ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes by user, not by root, -1 means path is null, 0 means path is not exist.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static long getUsableSpace(File path) {
        if (path == null) {
            return -1;
        }
        if (Version.hasGingerbread()) {
            return path.getUsableSpace();
        } else {
            if (!path.exists()) {
                return 0;
            } else {
                final StatFs stats = new StatFs(path.getPath());
                return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
            }
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static long getUsedSpace(File path) {
        if (path == null) {
            return -1;
        }

        if (Version.hasGingerbread()) {
            return path.getTotalSpace() - path.getUsableSpace();
        } else {
            if (!path.exists()) {
                return -1;
            } else {
                final StatFs stats = new StatFs(path.getPath());
                return (long) stats.getBlockSize() * (stats.getBlockCount() - stats.getAvailableBlocks());
            }
        }
    }

    /**
     * @param path
     * @return -1 means path is null, 0 means path is not exist.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static long getTotalSpace(File path) {
        if (path == null) {
            return -1;
        }
        if (Version.hasGingerbread()) {
            return path.getTotalSpace();
        } else {
            if (!path.exists()) {
                return 0;
            } else {
                final StatFs stats = new StatFs(path.getPath());
                return (long) stats.getBlockSize() * (long) stats.getBlockCount();
            }
        }
    }

    public static boolean hasSDCardMounted() {
        String state = Environment.getExternalStorageState();
        if (state != null && state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * external: "/storage/emulated/0/Android/data/in.srain.sample/files"
     * internal: "/data/data/in.srain.sample/files"
     */
    public static String wantFilesPath(Context context, boolean externalStorageFirst) {
        String path = null;
        File f = null;
        if (externalStorageFirst && DiskFileUtils.hasSDCardMounted() && (f = context.getExternalFilesDir("xxx")) != null) {
            path = f.getAbsolutePath();
        } else {
            path = context.getFilesDir().getAbsolutePath();
        }
        return path;
    }

    /**
     * @param context
     * @param filePath file path relative to assets, like request_init1/search_index.json
     * @return
     */
    public static String readAssert(Context context, String filePath) {
        try {
            if (filePath.startsWith(File.separator)) {
                filePath = filePath.substring(File.separator.length());
            }
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(filePath);
            DataInputStream stream = new DataInputStream(inputStream);
            int length = stream.available();
            byte[] buffer = new byte[length];
            stream.readFully(buffer);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(buffer);
            stream.close();
            return byteArrayOutputStream.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class CacheDirInfo {
        public File path;
        public boolean isInternal = false;
        public boolean isNotEnough = false;
        public long realSize;
        public long requireSize;
    }
}
