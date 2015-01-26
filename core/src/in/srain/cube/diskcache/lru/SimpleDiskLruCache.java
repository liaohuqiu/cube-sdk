/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.srain.cube.diskcache.lru;

import in.srain.cube.diskcache.CacheEntry;
import in.srain.cube.diskcache.DiskCache;
import in.srain.cube.util.CLog;

import java.io.File;
import java.io.IOException;

public final class SimpleDiskLruCache implements DiskCache {

    public static final String LOG_TAG = "cube-disk-cache-simple-lru";
    public static boolean DEBUG = false;
    private String mString;

    private LruActionTracer mActionTracer;

    /**
     * @param directory
     * @param appVersion
     * @param capacity
     */
    public SimpleDiskLruCache(File directory, int appVersion, long capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity <= 0");
        }
        mActionTracer = new LruActionTracer(this, directory, appVersion, capacity);
        if (DEBUG) {
            CLog.d(LOG_TAG, "Construct: path: %s version: %s capacity: %s", directory, appVersion, capacity);
        }
    }

    /**
     * clear all the content
     */
    @Override
    public synchronized void clear() throws IOException {
        mActionTracer.clear();
    }

    @Override
    public boolean has(String key) {
        return mActionTracer.has(key);
    }

    @Override
    public synchronized void open() throws IOException {
        mActionTracer.tryToResume();
    }

    /**
     * Returns a {@link in.srain.cube.diskcache.CacheEntry} named {@code key}, or null if it doesn't
     * exist is not currently readable. If a value is returned, it is moved to
     * the head of the LRU queue.
     */
    @Override
    public synchronized CacheEntry getEntry(String key) throws IOException {
        return mActionTracer.getEntry(key);
    }

    @Override
    public synchronized CacheEntry beginEdit(String key) throws IOException {
        return mActionTracer.beginEdit(key);
    }

    @Override
    public void abortEdit(CacheEntry cacheEntry) {
        mActionTracer.abortEdit(cacheEntry);
    }

    @Override
    public void abortEdit(String key) {
        mActionTracer.abortEdit(key);
    }

    @Override
    public void commitEdit(CacheEntry cacheEntry) throws IOException {
        mActionTracer.commitEdit(cacheEntry);
    }

    /**
     * Drops the entry for {@code key} if it exists and can be removed. Entries
     * actively being edited cannot be removed.
     *
     * @return true if an entry was removed.
     */
    public synchronized boolean delete(String key) throws IOException {
        return mActionTracer.delete(key);
    }

    @Override
    public long getCapacity() {
        return mActionTracer.getCapacity();
    }

    /**
     * Returns the number of bytes currently being used to store the values in
     * this cache. This may be greater than the max size if a background
     * deletion is pending.
     */
    @Override
    public synchronized long getSize() {
        return mActionTracer.getSize();
    }

    /**
     * Returns the directory where this cache stores its data.
     */
    public File getDirectory() {
        return mActionTracer.getDirectory();
    }

    /**
     * Force buffered operations to the filesystem.
     */
    @Override
    public synchronized void flush() throws IOException {
        mActionTracer.flush();
    }

    /**
     * Closes this cache. Stored values will remain on the filesystem.
     */
    @Override
    public synchronized void close() throws IOException {
        mActionTracer.close();
    }

    @Override
    public String toString() {
        if (mString == null) {
            mString = String.format("[SimpleDiskLruCache/%s@%s]", getDirectory().getName(), Integer.toHexString(hashCode()));
        }
        return mString;
    }
}
