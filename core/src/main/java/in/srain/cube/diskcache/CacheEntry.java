package in.srain.cube.diskcache;

import java.io.*;
import java.nio.charset.Charset;

public class CacheEntry {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final String mKey;
    private DiskCache mDiskCache;
    private long mOldSize;
    private long mSize;
    private boolean mIsUnderEdit;
    private boolean hasErrors;

    public CacheEntry(DiskCache diskCache, String key) {
        mDiskCache = diskCache;
        this.mKey = key;
    }

    private static String inputStreamToString(InputStream in) throws IOException {
        return FileUtils.readFully(new InputStreamReader(in, UTF_8));
    }

    public boolean isUnderEdit() {
        return mIsUnderEdit;
    }

    public String getKey() {
        return mKey;
    }

    public File getCacheFile() {
        return new File(mDiskCache.getDirectory(), mKey);
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public long getLastSize() {
        return mOldSize;
    }

    public File getTempFile() {
        return new File(mDiskCache.getDirectory(), mKey + ".tmp");
    }

    public boolean isReadable() {
        return getCacheFile().exists();
    }

    /**
     * Returns an unbuffered input stream to read the last committed value,
     * or null if no value has been committed.
     */
    public InputStream getInputStream() throws IOException {
        synchronized (mDiskCache) {
            if (!isReadable()) {
                return null;
            }
            return new FileInputStream(getCacheFile());
        }
    }

    /**
     * Returns the last committed value as a string, or null if no value
     * has been committed.
     */
    public String getString() throws IOException {
        InputStream in = getInputStream();
        return in != null ? inputStreamToString(in) : null;
    }

    /**
     * Sets the value
     */
    public CacheEntry setString(String value) throws IOException {
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(newOutputStream(), UTF_8);
            writer.write(value);
        } finally {
            FileUtils.closeQuietly(writer);
        }
        return this;
    }

    /**
     * Returns a new unbuffered output stream to write the value
     * If the underlying output stream encounters errors
     * when writing to the filesystem, this edit will be aborted when
     * {@link #commit} is called. The returned output stream does not throw
     * IOExceptions.
     */
    public OutputStream newOutputStream() throws IOException {
        synchronized (mDiskCache) {
            if (mIsUnderEdit) {
                throw new IOException("This file has been under edit");
            }
            mIsUnderEdit = true;
            File tempFile = getTempFile();
            File parent = tempFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Can not make sure the parent directory exist.");
            }
            return new FaultHidingOutputStream(new FileOutputStream(tempFile));
        }
    }

    /**
     * delete all content
     *
     * @return
     */
    public boolean delete() throws IOException {
        if (mIsUnderEdit) {
            throw new IOException("Try to delete an cache entry that has been being editing.");
        }
        FileUtils.deleteIfExists(getCacheFile());
        FileUtils.deleteIfExists(getTempFile());
        return true;
    }

    /**
     * Commits this edit so it is visible to readers.  This releases the
     * edit lock so another edit may be started on the same key.
     */
    public void commit() throws IOException {
        if (!mIsUnderEdit) {
            throw new IOException("CacheEntry has been closed.");
        }
        if (hasErrors) {
            mDiskCache.delete(mKey);
        } else {
            File dirty = getTempFile();
            if (dirty.exists()) {
                File clean = getCacheFile();
                dirty.renameTo(clean);
                mOldSize = mSize;
                mSize = clean.length();
                mDiskCache.commitEdit(this);
            } else {
                abortEdit();
            }
        }
        mIsUnderEdit = false;
    }

    /*
     * Aborts this edit. This releases the edit lock so another edit may be
     * started on the same mKey.
     */
    public synchronized void abortEdit() throws IOException {
        if (!mIsUnderEdit) {
            return;
        }
        mIsUnderEdit = false;
        FileUtils.deleteIfExists(getTempFile());
        mDiskCache.abortEdit(this);
    }

    private class FaultHidingOutputStream extends FilterOutputStream {
        private FaultHidingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(int oneByte) {
            try {
                out.write(oneByte);
            } catch (IOException e) {
                hasErrors = true;
            }
        }

        @Override
        public void write(byte[] buffer, int offset, int length) {
            try {
                out.write(buffer, offset, length);
            } catch (IOException e) {
                hasErrors = true;
            }
        }

        @Override
        public void close() {
            try {
                out.close();
            } catch (IOException e) {
                hasErrors = true;
            }
        }

        @Override
        public void flush() {
            try {
                out.flush();
            } catch (IOException e) {
                hasErrors = true;
            }
        }
    }
}

