package net.liaohuqiu.cube.file;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.StatFs;

import net.liaohuqiu.cube.util.Version;

public class FileUtil {

	/**
	 * Get a usable cache directory (external if available, internal otherwise).
	 * 
	 * @param context
	 *            The context to use
	 * @param uniqueName
	 *            A unique folder name to append to the cache folder
	 * @return The cache folder
	 */
	public static File getDiskCacheDir(Context context, String uniqueName, int requireSpace) {
		// Check if media is mounted or storage is built-in, if so, try and use external cache folder
		// otherwise use internal cache folder
		File sdPath = null;
		File internalPath = null;
		File cacheFile = null;
		Long sdCardFree = 0L;
		Long internalFree = 0L;
		if (hasSDCardMounted()) {
			sdPath = getExternalCacheDir(context);
			sdCardFree = getUsableSpace(sdPath);
		}
		if (sdPath == null || sdCardFree < requireSpace) {
			internalPath = context.getCacheDir();
			internalFree = getUsableSpace(internalPath);

			if (internalFree < requireSpace) {

				cacheFile = internalFree > sdCardFree ? internalPath : sdPath;

			} else {
				cacheFile = internalPath;
			}

		} else {
			cacheFile = sdPath;
		}

		String cachePath = cacheFile.getPath();
		return new File(cachePath + File.separator + uniqueName);
	}

	/**
	 * Get the external application cache directory.
	 * 
	 * @param context
	 *            The context to use
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
	 * @param path
	 *            The path to check
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

	/**
	 * 
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

	public static String wantFilesPath(Context context, boolean externalStorageFirst, String specifiedPathForExtenalStoage) {

		String path = null;
		if (externalStorageFirst && hasSDCardMounted()) {
			if (specifiedPathForExtenalStoage != null && specifiedPathForExtenalStoage.length() != 0)
				path = Environment.getExternalStorageDirectory() + "/" + specifiedPathForExtenalStoage + "/files";
			else {
				path = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/files";
			}
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
	 * 
	 * @param context
	 * @param filePath
	 *            file path relative to assets, like request_init1/search_index.json
	 * 
	 * @return
	 */
	public static String readAssert(Context context, String filePath) {
		try {
			AssetManager assetManager = context.getAssets();
			InputStream inputStream = assetManager.open(filePath);
			DataInputStream stream = new DataInputStream(inputStream);
			int length = stream.available();
			byte[] buffer = new byte[length];
			stream.readFully(buffer);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(buffer);
			return byteArrayOutputStream.toString();
		} catch (Exception e) {
		}
		return "";
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
}