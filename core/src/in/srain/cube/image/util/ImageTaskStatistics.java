package in.srain.cube.image.util;

/**
 * Created by srain on 9/15/14.
 */
public class ImageTaskStatistics {

    private long mStart;
    private long mAfterCache;
    private long mAfterDownload;
    private long mAfterDecode;
    private long mDone;

    public ImageTaskStatistics() {
        mStart = System.currentTimeMillis();
    }

    public void afterCache() {
        mAfterCache = System.currentTimeMillis();
    }

    public void beginLoad() {

    }

    public void afterDownload() {

    }

    public void afterDecode() {

    }
}
