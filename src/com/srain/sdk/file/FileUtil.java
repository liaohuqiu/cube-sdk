package com.srain.sdk.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.os.Environment;

public class FileUtil {

	public static boolean hasSDCardMounted() {
		String state = android.os.Environment.getExternalStorageState();
		if (state != null && state.equals(android.os.Environment.MEDIA_MOUNTED)) {
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

	public static String read(String fileName) {
		FileInputStream fileInput = null;
		FileChannel channel = null;
		try {
			fileInput = new FileInputStream(fileName);
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
