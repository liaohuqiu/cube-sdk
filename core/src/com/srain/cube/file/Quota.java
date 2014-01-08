package com.srain.cube.file;

import android.os.Environment;
import android.os.StatFs;

public class Quota {

	/**
	 * Number of bytes in one KB = 2<sup>10</sup>
	 */
	public final static long SIZE_KB = 1024L;

	/**
	 * Number of bytes in one MB = 2<sup>20</sup>
	 */
	public final static long SIZE_MB = SIZE_KB * SIZE_KB;

	/**
	 * Number of bytes in one GB = 2<sup>30</sup>
	 */
	public final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

	/**
	 * @return Number of bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInBytes() {
		long availableSpace = -1L;
		try {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return availableSpace;
	}

	/**
	 * @return Number of kilo bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInKB() {
		return getExternalAvailableSpaceInBytes() / SIZE_KB;
	}

	/**
	 * @return Number of Mega bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInMB() {
		return getExternalAvailableSpaceInBytes() / SIZE_MB;
	}

	/**
	 * @return gega bytes of bytes available on external storage
	 */
	public static long getExternalAvailableSpaceInGB() {
		return getExternalAvailableSpaceInBytes() / SIZE_GB;
	}

	/**
	 * @return Total number of available blocks on external storage
	 */
	public static long getExternalStorageAvailableBlocks() {
		long availableBlocks = -1L;
		try {
			StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
			availableBlocks = stat.getAvailableBlocks();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return availableBlocks;
	}
}