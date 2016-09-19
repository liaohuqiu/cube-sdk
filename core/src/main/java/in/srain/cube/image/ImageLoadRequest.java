package in.srain.cube.image;

public class ImageLoadRequest {

    public String mUrl;

    private int mViewWidth;
    private int mViewHeight;
    private int mSpecifiedWidth;
    private int mSpecifiedHeight;

    private int mPriority;
    private ImageReuseInfo mImageReuseInfo;

    private static final int AUTO_DECIDE_LOAD_SIZE = 0x01;
    private int mFlag = AUTO_DECIDE_LOAD_SIZE;

    public static ImageLoadRequest create(String url) {
        ImageLoadRequest request = new ImageLoadRequest(url);
        return request;
    }

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
        return mViewWidth;
    }

    public ImageLoadRequest setLayoutSize(int w, int h) {
        mViewWidth = w;
        mViewHeight = h;
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
        if (mSpecifiedHeight != 0) {
            return mSpecifiedHeight;
        }
        return mViewHeight;
    }

    public ImageLoadRequest setAutoDecideLoadSize(boolean autoDecideSize) {
        if (autoDecideSize) {
            mFlag |= AUTO_DECIDE_LOAD_SIZE;
        } else {
            mFlag &= ~AUTO_DECIDE_LOAD_SIZE;
        }
        return this;
    }

    public boolean autoDecideLoadSize() {
        return (mFlag & AUTO_DECIDE_LOAD_SIZE) != 0;
    }
}
