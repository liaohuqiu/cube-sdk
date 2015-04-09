package in.srain.cube.image;

/**
 * Created by xufu.lg on 2015/3/30.
 */
public class ImageLoadRequest {

    public String mUrl;

    private int mRequestWidth;
    private int mRequestHeight;
    private int mSpecifiedWidth;
    private int mSpecifiedHeight;
    private int mPriority;
    private ImageReuseInfo mImageReuseInfo;

    public ImageLoadRequest(String url, int specifiedWidth, int specifiedHeight, int priority, ImageReuseInfo reuseInfo) {
        mUrl = url;
        mSpecifiedWidth = specifiedWidth;
        mSpecifiedHeight = specifiedHeight;
        mPriority = priority;
        mImageReuseInfo = reuseInfo;
    }

    public ImageLoadRequest(String url) {
        mUrl = url;
    }

    public ImageLoadRequest(String url, int priority) {
        mUrl = url;
        mPriority = priority;
    }

    public int getRequestWidth() {
        if (mSpecifiedWidth != 0) {
            return mSpecifiedWidth;
        }
        return mRequestWidth;
    }

    public ImageLoadRequest setLayoutSize(int w, int h) {
        mRequestWidth = w;
        mRequestHeight = h;
        return this;
    }

    public String getUrl() {
        return mUrl;
    }

    public int getPriority() {
        return mPriority;
    }

    public ImageReuseInfo getImageReuseInfo() {
        return mImageReuseInfo;
    }

    public int getRequestHeight() {
        if (mRequestHeight != 0) {
            return mRequestHeight;
        }
        return mSpecifiedHeight;
    }
}
