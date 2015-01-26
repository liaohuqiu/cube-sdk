package in.srain.cube.diskcache;

import java.io.File;
import java.io.IOException;

public interface DiskCache {

    /**
     * Check if has this key
     *
     * @param key
     * @return
     */
    public boolean has(String key);

    /**
     * open disk cache
     *
     * @throws java.io.IOException
     */
    public void open() throws IOException;

    /**
     * clear all data
     *
     * @throws java.io.IOException
     */
    public void clear() throws IOException;

    /**
     * close the cache
     *
     * @throws java.io.IOException
     */
    public void close() throws IOException;

    /**
     * flush data to dish
     */
    public void flush() throws IOException;

    /**
     * @param key
     * @return
     * @throws java.io.IOException
     */
    public CacheEntry getEntry(String key) throws IOException;

    /**
     * begin edit an {@CacheEntry }
     *
     * @param key
     * @return
     * @throws java.io.IOException
     */
    public CacheEntry beginEdit(String key) throws IOException;

    /**
     * abort edit
     *
     * @param cacheEntry
     */
    public void abortEdit(CacheEntry cacheEntry);

    /**
     * abort edit by key
     *
     * @param key
     */
    public void abortEdit(String key);

    /**
     * abort edit by key
     */
    public void commitEdit(CacheEntry cacheEntry) throws IOException;

    /**
     * delete if key exist, under edit can not be deleted
     *
     * @param key
     * @return
     */
    public boolean delete(String key) throws IOException;

    public long getCapacity();

    public long getSize();

    public File getDirectory();
}
