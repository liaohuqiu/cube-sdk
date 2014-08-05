package net.liaohuqiu.cube.image.iface;

import java.io.InputStream;

import android.graphics.Bitmap;

public interface ImageFileCache {

	public void write(String fileCacheKey, Bitmap bitmap);

	public InputStream read(String fileCacheKey);
}
