package in.srain.cube.request;

public class FailData {

    public static final int TYPE_DATA_FORMAT_ERROR = 1;
    public static final int TYPE_CUSTOMIZE_ERROR = 2;

    public int mErrorType = TYPE_DATA_FORMAT_ERROR;
    public int mCustomErrorType;
    public Object mData;

    public static FailData dataFormatError(String content) {
        return new FailData(TYPE_DATA_FORMAT_ERROR, -1, content);
    }

    public static FailData customizedError(int error, Object data) {
        return new FailData(TYPE_CUSTOMIZE_ERROR, error, data);
    }

    private FailData(int errorType, int customErrorType, Object data) {
        mErrorType = errorType;
        mCustomErrorType = customErrorType;
        mData = data;
    }

    public int getErrorType() {
        return mErrorType;
    }

    public int getCustomErrorType() {
        return mCustomErrorType;
    }

    public <T> T getData(Class<T> cls) {
        if (mData == null || !cls.isInstance(mData)) {
            return null;
        }
        return (T) mData;
    }
}
