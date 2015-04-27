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

import android.util.Log;
import in.srain.cube.diskcache.CacheEntry;
import in.srain.cube.diskcache.DiskCache;
import in.srain.cube.diskcache.FileUtils;
import in.srain.cube.set.hash.SimpleHashSet;
import in.srain.cube.util.CLog;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public final class LruActionTracer implements Runnable {

    final static int REDUNDANT_OP_COMPACT_THRESHOLD = 2000;
    static final String JOURNAL_FILE = "journal";
    static final String JOURNAL_FILE_TMP = "journal.tmp";
    static final String MAGIC = "lru-tracer";
    static final String VERSION_1 = "1";

    private static final byte ACTION_CLEAN = 1;
    private static final byte ACTION_DIRTY = 2;
    private static final byte ACTION_DELETE = 3;
    private static final byte ACTION_READ = 4;
    private static final byte ACTION_PENDING_DELETE = 5;
    private static final byte ACTION_FLUSH = 6;

    private static final String[] sACTION_LIST = new String[]{"UN_KNOW", "CLEAN", "DIRTY", "DELETE", "READ", "DELETE_PENDING", "FLUSH"};

    private static final int IO_BUFFER_SIZE = 8 * 1024;

    private static final byte[] sPoolSync = new byte[0];
    private static final int MAX_POOL_SIZE = 50;
    private static ActionMessage sPoolHeader;
    private static int sPoolSize = 0;
    private final LinkedHashMap<String, CacheEntry> mLruEntries
            = new LinkedHashMap<String, CacheEntry>(0, 0.75f, true);
    /**
     * This cache uses a single background thread to evict entries.
     */
    private final ExecutorService mExecutorService = new ThreadPoolExecutor(0, 1,
            60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    private final File mJournalFile;
    private final File mJournalFileTmp;
    private boolean mIsRunning = false;
    private DiskCache mDiskCache;
    private long mSize = 0;
    private ConcurrentLinkedQueue<ActionMessage> mActionQueue;

    private File mDirectory;
    private long mCapacity;
    private int mAppVersion;
    private SimpleHashSet mNewCreateList;
    private Object mLock = new Object();
    private Writer mJournalWriter;
    private int mRedundantOpCount;
    private HashMap<String, CacheEntry> mEditList;

    public LruActionTracer(DiskCache diskCache, File directory, int appVersion, long capacity) {
        mDiskCache = diskCache;
        mJournalFile = new File(directory, JOURNAL_FILE);
        mJournalFileTmp = new File(directory, JOURNAL_FILE_TMP);

        mDirectory = directory;
        mAppVersion = appVersion;
        mCapacity = capacity;
        mNewCreateList = new SimpleHashSet();
        mEditList = new HashMap<String, CacheEntry>();
        mActionQueue = new ConcurrentLinkedQueue<ActionMessage>();
    }

    private static void validateKey(String key) {
        if (key.contains(" ") || key.contains("\n") || key.contains("\r")) {
            throw new IllegalArgumentException(
                    "keys must not contain spaces or newlines: \"" + key + "\"");
        }
    }

    /**
     * try to resume last status when we got off
     *
     * @throws java.io.IOException
     */
    public void tryToResume() throws IOException {
        if (mJournalFile.exists()) {
            try {
                readJournal();
                processJournal();
                mJournalWriter = new BufferedWriter(new FileWriter(mJournalFile, true),
                        IO_BUFFER_SIZE);
                if (SimpleDiskLruCache.DEBUG) {
                    CLog.d(SimpleDiskLruCache.LOG_TAG, "open success");
                }
            } catch (IOException journalIsCorrupt) {
                journalIsCorrupt.printStackTrace();
                if (SimpleDiskLruCache.DEBUG) {
                    CLog.d(SimpleDiskLruCache.LOG_TAG, "clear old cache");
                }
                clear();
            }
        } else {

            if (SimpleDiskLruCache.DEBUG) {
                CLog.d(SimpleDiskLruCache.LOG_TAG, "create new cache");
            }

            // create a new empty cache
            if (mDirectory.exists()) {
                mDirectory.delete();
            }
            mDirectory.mkdirs();
            rebuildJournal();
        }
    }

    public synchronized void clear() throws IOException {

        // abort edit
        for (CacheEntry cacheEntry : new ArrayList<CacheEntry>(mLruEntries.values())) {
            if (cacheEntry.isUnderEdit()) {
                cacheEntry.abortEdit();
            }
        }
        mLruEntries.clear();
        mSize = 0;

        // delete current directory then rebuild
        if (SimpleDiskLruCache.DEBUG) {
            CLog.d(SimpleDiskLruCache.LOG_TAG, "delete directory");
        }

        waitJobDone();

        // rebuild
        FileUtils.deleteDirectoryQuickly(mDirectory);
        rebuildJournal();
    }

    /**
     * Returns a {@link in.srain.cube.diskcache.CacheEntry} named {@code key}, or null if it doesn't
     * exist is not currently readable. If a value is returned, it is moved to
     * the head of the LRU queue.
     */
    public synchronized CacheEntry getEntry(String key) throws IOException {
        checkNotClosed();
        validateKey(key);
        CacheEntry cacheEntry = mLruEntries.get(key);
        if (cacheEntry == null) {
            return null;
        }

        trimToSize();
        addActionLog(ACTION_READ, cacheEntry);
        return cacheEntry;
    }

    public synchronized CacheEntry beginEdit(String key) throws IOException {
        checkNotClosed();
        validateKey(key);

        if (SimpleDiskLruCache.DEBUG) {
            CLog.d(SimpleDiskLruCache.LOG_TAG, "beginEdit: %s", key);
        }
        CacheEntry cacheEntry = mLruEntries.get(key);
        if (cacheEntry == null) {
            cacheEntry = new CacheEntry(mDiskCache, key);
            mNewCreateList.add(key);
            mLruEntries.put(key, cacheEntry);
        }
        mEditList.put(key, cacheEntry);

        addActionLog(ACTION_DIRTY, cacheEntry);
        return cacheEntry;
    }

    public void abortEdit(String key) {
        CacheEntry cacheEntry = mEditList.get(key);
        if (cacheEntry != null) {
            try {
                cacheEntry.abortEdit();
            } catch (IOException e) {
            }
        }
    }

    public void abortEdit(CacheEntry cacheEntry) {
        final String cacheKey = cacheEntry.getKey();
        if (SimpleDiskLruCache.DEBUG) {
            CLog.d(SimpleDiskLruCache.LOG_TAG, "abortEdit: %s", cacheKey);
        }
        if (mNewCreateList.contains(cacheKey)) {
            mLruEntries.remove(cacheKey);
            mNewCreateList.remove(cacheKey);
        }
        mEditList.remove(cacheKey);
    }

    public void commitEdit(CacheEntry cacheEntry) throws IOException {
        if (SimpleDiskLruCache.DEBUG) {
            CLog.d(SimpleDiskLruCache.LOG_TAG, "commitEdit: %s", cacheEntry.getKey());
        }

        mNewCreateList.remove(cacheEntry.getKey());
        mEditList.remove(cacheEntry.getKey());

        mSize += cacheEntry.getSize() - cacheEntry.getLastSize();
        addActionLog(ACTION_CLEAN, cacheEntry);
        trimToSize();
    }

    private void readJournalLine(String line) throws IOException {
        String[] parts = line.split(" ");
        if (parts.length < 2) {
            throw new IOException("unexpected journal line: " + line);
        }

        if (parts.length != 3) {
            throw new IOException("unexpected journal line: " + line);
        }

        String key = parts[1];
        if (parts[0].equals(sACTION_LIST[ACTION_DELETE])) {
            mLruEntries.remove(key);
            return;
        }

        CacheEntry cacheEntry = mLruEntries.get(key);
        if (cacheEntry == null) {
            cacheEntry = new CacheEntry(mDiskCache, key);
            mLruEntries.put(key, cacheEntry);
        }

        if (parts[0].equals(sACTION_LIST[ACTION_CLEAN])) {
            cacheEntry.setSize(Long.parseLong(parts[2]));
        } else if (parts[0].equals(sACTION_LIST[ACTION_DIRTY])) {
            // skip
        } else if (parts[0].equals(sACTION_LIST[ACTION_READ])) {
            // this work was already done by calling mLruEntries.get()
        } else {
            throw new IOException("unexpected journal line: " + line);
        }
    }

    /**
     * Computes the initial size and collects garbage as a part of opening the
     * cache. Dirty entries are assumed to be inconsistent and will be deleted.
     */
    private void processJournal() throws IOException {
        FileUtils.deleteIfExists(mJournalFileTmp);
        for (Iterator<CacheEntry> i = mLruEntries.values().iterator(); i.hasNext(); ) {
            CacheEntry cacheEntry = i.next();
            if (!cacheEntry.isUnderEdit()) {
                mSize += cacheEntry.getSize();
            } else {
                cacheEntry.delete();
                i.remove();
            }
        }
    }

    /**
     * Creates a new journal that omits redundant information. This replaces the
     * current journal if it exists.
     */
    private void rebuildJournal() throws IOException {
        if (mJournalWriter != null) {
            mJournalWriter.close();
        }

        Writer writer = new BufferedWriter(new FileWriter(mJournalFileTmp), IO_BUFFER_SIZE);
        writer.write(MAGIC);
        writer.write("\n");
        writer.write(VERSION_1);
        writer.write("\n");
        writer.write(Integer.toString(mAppVersion));
        writer.write("\n");
        writer.write("\n");

        for (CacheEntry cacheEntry : mLruEntries.values()) {
            if (cacheEntry.isUnderEdit()) {
                writer.write(sACTION_LIST[ACTION_DIRTY] + ' ' + cacheEntry.getKey() + " " + cacheEntry.getSize() + '\n');
            } else {
                writer.write(sACTION_LIST[ACTION_CLEAN] + ' ' + cacheEntry.getKey() + " " + cacheEntry.getSize() + '\n');
            }
        }

        writer.close();
        mJournalFileTmp.renameTo(mJournalFile);
        mJournalWriter = new BufferedWriter(new FileWriter(mJournalFile, true), IO_BUFFER_SIZE);
    }

    private void readJournal() throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(mJournalFile), IO_BUFFER_SIZE);
        try {
            String magic = FileUtils.readAsciiLine(in);
            String version = FileUtils.readAsciiLine(in);
            String appVersionString = FileUtils.readAsciiLine(in);
            String blank = FileUtils.readAsciiLine(in);
            if (!MAGIC.equals(magic)
                    || !VERSION_1.equals(version)
                    || !Integer.toString(mAppVersion).equals(appVersionString)
                    || !"".equals(blank)) {
                throw new IOException("unexpected journal header: ["
                        + magic + ", " + version + ", " + blank + "]");
            }

            while (true) {
                try {
                    readJournalLine(FileUtils.readAsciiLine(in));
                } catch (EOFException endOfJournal) {
                    break;
                }
            }
        } finally {
            FileUtils.closeQuietly(in);
        }
    }

    private void checkNotClosed() {
        if (mJournalFile == null) {
            throw new IllegalStateException("cache is closed");
        }
    }

    /**
     * Force buffered operations to the filesystem.
     */
    public synchronized void flush() throws IOException {
        checkNotClosed();
        trimToSize();
        addActionLog(ACTION_FLUSH, null);
        waitJobDone();
    }

    private void writeActionLog(byte action, CacheEntry cacheEntry) throws IOException {
        mJournalWriter.write(sACTION_LIST[action] + ' ' + cacheEntry.getKey() + ' ' + cacheEntry.getSize() + '\n');
        mRedundantOpCount++;
        if (mRedundantOpCount >= REDUNDANT_OP_COMPACT_THRESHOLD && mRedundantOpCount >= mLruEntries.size()) {
            mRedundantOpCount = 0;
            rebuildJournal();
        }
    }

    private void doJob() {
        synchronized (mLock) {
            while (!mActionQueue.isEmpty()) {
                try {
                    ActionMessage message = mActionQueue.poll();
                    doUntil(message);
                    // if every thread get a IOexception, and mLock.notify would not call. so in sometimes this will cause mLock's deadlock
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mLock.notify();
        }
    }

    private void doUntil(ActionMessage message) throws IOException {
        final CacheEntry cacheEntry = message.mCacheEntry;
        final byte action = message.mAction;
        message.recycle();

        if (SimpleDiskLruCache.DEBUG) {
            CLog.d(SimpleDiskLruCache.LOG_TAG, "doAction: %s, key: %s",
                    sACTION_LIST[action], cacheEntry != null ? cacheEntry.getKey() : null);
        }

        switch (action) {
            case ACTION_READ:
                writeActionLog(action, cacheEntry);
                break;

            case ACTION_DIRTY:
                writeActionLog(action, cacheEntry);
                break;

            case ACTION_CLEAN:
                writeActionLog(action, cacheEntry);
                break;

            case ACTION_DELETE:
                writeActionLog(action, cacheEntry);
                break;

            case ACTION_PENDING_DELETE:
                writeActionLog(action, cacheEntry);
                if (mLruEntries.containsKey(cacheEntry.getKey())) {
                    return;
                }
                cacheEntry.delete();
                break;
            case ACTION_FLUSH:
                mJournalWriter.flush();
                break;
        }
    }

    private void waitJobDone() {
        if (SimpleDiskLruCache.DEBUG) {
            CLog.d(SimpleDiskLruCache.LOG_TAG, "waitJobDone");
        }

        // remove synchronized method , exclude this code block for dead lock digging
        synchronized (mLock) {
            if (mIsRunning) {
                while (!mActionQueue.isEmpty()) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (SimpleDiskLruCache.DEBUG) {
            CLog.d(SimpleDiskLruCache.LOG_TAG, "job is done");
        }
    }

    private void addActionLog(byte action, CacheEntry cacheEntry) {
        mActionQueue.add(ActionMessage.obtain(action, cacheEntry));
        if (!mIsRunning) {
            mIsRunning = true;
            mExecutorService.submit(this);
        }
    }

    public synchronized void close() throws IOException {
        if (isClosed()) {
            return; // already closed
        }
        for (CacheEntry cacheEntry : new ArrayList<CacheEntry>(mLruEntries.values())) {
            if (cacheEntry.isUnderEdit()) {
                cacheEntry.abortEdit();
            }
        }
        trimToSize();
        waitJobDone();
        rebuildJournal();
        mJournalWriter.close();
        mJournalWriter = null;
    }

    private boolean isClosed() {
        return mJournalWriter == null;
    }

    @Override
    public void run() {
        doJob();
        mIsRunning = false;
    }

    /**
     * remove files from list, delete files
     */
    private synchronized void trimToSize() {

        if (mSize > mCapacity) {
            if (SimpleDiskLruCache.DEBUG) {
                CLog.d(SimpleDiskLruCache.LOG_TAG, "should trim, current is: %s", mSize);
            }
        }
        while (mSize > mCapacity) {
            Map.Entry<String, CacheEntry> toEvict = mLruEntries.entrySet().iterator().next();
            String key = toEvict.getKey();
            CacheEntry cacheEntry = toEvict.getValue();
            mLruEntries.remove(key);

            mSize -= cacheEntry.getSize();
            addActionLog(ACTION_PENDING_DELETE, cacheEntry);
            if (SimpleDiskLruCache.DEBUG) {
                CLog.d(SimpleDiskLruCache.LOG_TAG, "pending remove: %s, size: %s, after remove total: %s", key, cacheEntry.getSize(), mSize);
            }
        }
    }

    public synchronized boolean delete(String key) throws IOException {
        if (SimpleDiskLruCache.DEBUG) {
            CLog.d(SimpleDiskLruCache.LOG_TAG, "delete: %s", key);
        }
        checkNotClosed();
        validateKey(key);
        CacheEntry cacheEntry = mLruEntries.get(key);
        if (cacheEntry == null) {
            return false;
        }

        // delete at once
        cacheEntry.delete();
        mSize -= cacheEntry.getSize();
        cacheEntry.setSize(0);
        mLruEntries.remove(key);

        addActionLog(ACTION_DELETE, cacheEntry);
        return true;
    }

    public long getSize() {
        return mSize;
    }

    public long getCapacity() {
        return mCapacity;
    }

    public File getDirectory() {
        return mDirectory;
    }

    public boolean has(String key) {
        return mLruEntries.containsKey(key) && !mNewCreateList.contains(key);
    }

    private static class ActionMessage {
        private byte mAction;
        private CacheEntry mCacheEntry;
        private ActionMessage mNext;

        public ActionMessage(byte action, CacheEntry cacheEntry) {
            mAction = action;
            mCacheEntry = cacheEntry;
        }

        public static ActionMessage obtain(byte action, CacheEntry cacheEntry) {
            synchronized (sPoolSync) {
                if (sPoolHeader != null) {
                    ActionMessage m = sPoolHeader;

                    sPoolHeader = m.mNext;
                    m.mNext = null;
                    sPoolSize--;

                    m.mAction = action;
                    m.mCacheEntry = cacheEntry;
                    return m;
                }
            }
            return new ActionMessage(action, cacheEntry);
        }

        public void recycle() {
            mAction = 0;
            mCacheEntry = null;
            synchronized (sPoolSync) {
                if (sPoolSize < MAX_POOL_SIZE) {
                    mNext = sPoolHeader;
                    sPoolHeader = this;
                    sPoolSize++;
                }
            }
        }
    }
}
