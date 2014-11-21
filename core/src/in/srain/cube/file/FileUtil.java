package in.srain.cube.file;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StatFs;
import in.srain.cube.util.Version;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtil {

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
        return cacheDirInfo;
    }

    /**
     * Get the external application cache directory.
     *
     * @param context The context to use
     * @return The external cache folder : /storage/sdcard0/Android/data/com.srain.sdk/cache
     */
    @TargetApi(VERSION_CODES.FROYO)
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
    @TargetApi(VERSION_CODES.GINGERBREAD)
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
    @TargetApi(VERSION_CODES.GINGERBREAD)
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
        if (externalStorageFirst && hasSDCardMounted()) {
            path = context.getExternalFilesDir("").getAbsolutePath();
        } else {
            path = context.getFilesDir().getAbsolutePath();
        }
        return path;
    }

    public static File wantFile(String dir, String fileName) {
        File wallpaperDirectory = new File(dir);
        wallpaperDirectory.mkdirs();
        File outputFile = new File(wallpaperDirectory, fileName);
        return outputFile;
    }

    public static boolean write(String filePath, String content) {
        File file = new File(filePath);
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();

        FileWriter writer = null;
        try {

            writer = new FileWriter(file);
            writer.write(content);

        } catch (IOException e) {
        } finally {
            try {
                if (writer != null) {

                    writer.close();
                    return true;
                }
            } catch (IOException e) {
            }
        }
        return false;
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

    public static String read(String filePath) {
        File file = new File(filePath);
        if (!file.exists())
            return null;

        FileInputStream fileInput = null;
        FileChannel channel = null;
        try {
            fileInput = new FileInputStream(filePath);
            channel = fileInput.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(buffer.array());
            return byteArrayOutputStream.toString();
        } catch (Exception e) {
        } finally {

            if (fileInput != null) {
                try {
                    fileInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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