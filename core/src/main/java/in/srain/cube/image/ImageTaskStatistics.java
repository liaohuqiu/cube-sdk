package in.srain.cube.image;

/**
 * Created by srain on 9/15/14.
 */
public class ImageTaskStatistics {


    private long m0Start;
    private long m1BeginLoad;
    private long m2AfterCheckFileCache;
    private long m3AfterDownload;
    private long m4AfterDecode;
    private long m5ShowStart;
    private long m6ShowComplete;

    /**
     * in byte
     */
    private long mImageFileSize;
    private long mBitmapDrawableSize;

    private boolean mHitMemoryCache;
    private boolean mHitFileCache;

    public ImageTaskStatistics() {
        m0Start = System.currentTimeMillis();
    }

    public void s0_afterCheckMemoryCache(boolean hasCache) {
        mHitMemoryCache = hasCache;
        if (hasCache) {
            m1BeginLoad = m2AfterCheckFileCache = m3AfterDownload = m4AfterDecode = System.currentTimeMillis();
        }
    }

    public void s1_beginLoad() {
        m1BeginLoad = System.currentTimeMillis();
    }

    public void s2_afterCheckFileCache(boolean hasCache) {
        mHitFileCache = hasCache;
        m2AfterCheckFileCache = System.currentTimeMillis();
        if (hasCache) {
            m3AfterDownload = m2AfterCheckFileCache;
        }
    }

    public void s3_afterDownload() {
        m3AfterDownload = System.currentTimeMillis();
    }

    public void s4_afterDecode(long imageFileSize) {
        mImageFileSize = imageFileSize;
        m4AfterDecode = System.currentTimeMillis();
    }

    public void s5_beforeShow() {
        m5ShowStart = System.currentTimeMillis();
    }

    public void s6_afterShow(long s) {
        mBitmapDrawableSize = s;
        m6ShowComplete = System.currentTimeMillis();
    }

    /**
     * @return
     */
    @SuppressWarnings({"unused"})
    public long getBitmapDrawableSize() {
        return mBitmapDrawableSize;
    }

    /**
     * Decode from file cache
     *
     * @return
     */
    public int getDecodeTime() {
        return (int) (m4AfterDecode - m3AfterDownload);
    }

    /**
     * Download from remote server
     *
     * @return
     */
    public int getDownloadTime() {
        return (int) (m3AfterDownload - m2AfterCheckFileCache);
    }

    /**
     * check if has file cache
     *
     * @return
     */
    public int getCheckFileCacheTime() {
        return (int) (m2AfterCheckFileCache - m1BeginLoad);
    }

    @SuppressWarnings({"unused"})
    public long getImageFileSize() {
        return mImageFileSize;
    }

    /**
     * KillBytes / s
     *
     * @return -1 means did not do download
     */
    public int getDownLoadSpeed() {
        if (getDownloadTime() * mImageFileSize == 0) {
            return -1;
        }
        return (int) ((mImageFileSize >> 10) * 1000 / getDownloadTime());
    }

    public String getStatisticsInfo() {
        return String.format("mc=%d, fc=%d, wait_to_load=%d, check_file_cache=%d, download=%d/%dKB/s, decode=%d, wait_ui=%s, all=%d, size=%d/%d",
                mHitMemoryCache ? 1 : 0,
                mHitFileCache ? 1 : 0,
                getWaitForLoadTime(),
                getCheckFileCacheTime(),
                getDownloadTime(),
                getDownLoadSpeed(),
                getDecodeTime(),
                getWaitToPostMessage(),
                getTotalLoadTime(),
                mBitmapDrawableSize,
                mImageFileSize
        );
    }

    public int getTotalLoadTime() {
        return (int) (m6ShowComplete - m0Start);
    }

    public int getWaitForLoadTime() {
        return (int) (m1BeginLoad - m0Start);
    }

    public int getWaitToPostMessage() {
        return (int) (m5ShowStart - m4AfterDecode);
    }

    public boolean hitFileCache() {
        return mHitFileCache;
    }

    public boolean hitMemoryCache() {
        return mHitMemoryCache;
    }
}
