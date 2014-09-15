package in.srain.cube.image.iface;

import in.srain.cube.cache.IFileCache;

import java.io.InputStream;

public interface ImageFileCache extends IFileCache {
    public InputStream getInputStream(String key);

    public void writeInputStream(String key, InputStream stream);

    public void flushDiskCacheAsync();
}