package in.srain.cube.image.iface;

import in.srain.cube.cache.IFileCache;

import java.io.FileInputStream;

public interface ImageFileProvider extends IFileCache {

    public FileInputStream getInputStream(String key);

    public FileInputStream downloadAndGetInputStream(String key, String url);

    public void flushDiskCacheAsync();

    public void initDiskCacheAsync();
}