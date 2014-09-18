package in.srain.cube.image;

/**
 * Created by srain on 9/15/14.
 */
public class ImageTaskStatistics {

    private long mStart;
    private long mAfterMemoryCache;
    private long mBeginLoad;
    private long mAfterFileCache;
    private long mAfterDownload;
    private long mAfterDecode;
    private long mAfterCreateBitmapDrawable;
    private long mShowBegin;
    private long mShowComplete;
    private int mSize;
    private boolean mHitMemoryCache;
    private boolean mHitFileCache;

    public ImageTaskStatistics() {
        mStart = System.currentTimeMillis();
    }

    public void afterMemoryCache(boolean hasCache) {
        mHitMemoryCache = hasCache;
        mAfterMemoryCache = System.currentTimeMillis();
        if (hasCache) {
            mBeginLoad = mAfterFileCache = mAfterDownload = mAfterDecode = mAfterCreateBitmapDrawable = mAfterMemoryCache;
        }
    }

    public void beginLoad() {
        mBeginLoad = System.currentTimeMillis();
    }

    public void afterFileCache(boolean hasCache) {
        mHitFileCache = hasCache;
        mAfterFileCache = System.currentTimeMillis();
        if (hasCache) {
            mAfterDownload = mAfterFileCache;
        }
    }

    public void afterDownload() {
        mAfterDownload = System.currentTimeMillis();
    }

    public void afterDecode() {
        mAfterDecode = System.currentTimeMillis();
    }

    public void showBegin() {
        mShowBegin = System.currentTimeMillis();
    }

    public void afterCreateBitmapDrawable() {
        mAfterCreateBitmapDrawable = System.currentTimeMillis();
    }

    public void showComplete(int s) {
        mShowComplete = System.currentTimeMillis();
        mSize = s;
    }

    public int getMemoryCacheTime() {
        return (int) (mAfterMemoryCache - mStart);
    }

    public int getWaitForLoadTime() {
        return (int) (mBeginLoad - mAfterMemoryCache);
    }

    public int getFileCacheTime() {
        return (int) (mAfterFileCache - mBeginLoad);
    }

    public int getDownloadTime() {
        return (int) (mAfterDownload - mAfterFileCache);
    }

    public int getDecodeTime() {
        return (int) (mAfterDecode - mAfterDownload);
    }

    public int getCreateBitmapDrawableTime() {
        return (int) (mAfterCreateBitmapDrawable - mAfterDecode);
    }

    public int getWaitForPostMessage() {
        return (int) (mShowBegin - mAfterCreateBitmapDrawable);
    }

    public int getDisplayTime() {
        return (int) (mShowComplete - mShowBegin);
    }

    public int getTotalLoadTime() {
        return (int) (mShowComplete - mBeginLoad);
    }

    public boolean hitMemoryCache() {
        return mHitMemoryCache;
    }

    public boolean hitFileCache() {
        return mHitFileCache;
    }

    public int getSize() {
        return mSize;
    }

    public String getInfo() {
        return String.format("mc=%d, w=%d, fc=%d, dl=%d, de=%d, crt=%d, w2=%s, dis=%d, all=%d, size=%d",
                getMemoryCacheTime(),
                getWaitForLoadTime(),
                getFileCacheTime(),
                getDownloadTime(),
                getDecodeTime(),
                getCreateBitmapDrawableTime(),
                getWaitForPostMessage(),
                getDisplayTime(),
                getTotalLoadTime(),
                getSize()
        );
    }
}
