package in.srain.cube.request;

public class FailData {

    public static final int ERROR_UNKNOWN = 0;
    public static final int ERROR_INPUT = 1;
    public static final int ERROR_NETWORK = 2;
    public static final int ERROR_DATA_FORMAT = 3;
    public static final int ERROR_CUSTOMIZED = 100;

    public int mErrorType = ERROR_DATA_FORMAT;
    public int mCustomErrorType;
    public Object mData;

    public static FailData unknown() {
        return new FailData(ERROR_UNKNOWN, -1, null);
    }

    public static FailData inputError() {
        return new FailData(ERROR_INPUT, -1, null);
    }

    public static FailData networkError() {
        return new FailData(ERROR_NETWORK, -1, null);
    }

    public static FailData dataFormatError(String content) {
        return new FailData(ERROR_DATA_FORMAT, -1, content);
    }

    public static FailData customizedError(int error, Object data) {
        return new FailData(ERROR_CUSTOMIZED, error, data);
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
