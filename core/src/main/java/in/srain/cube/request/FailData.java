package in.srain.cube.request;

public class FailData {

    public static final int ERROR_UNKNOWN = 0;
    public static final int ERROR_INPUT = 1;
    public static final int ERROR_NETWORK = 2;
    public static final int ERROR_DATA_FORMAT = 3;
    public static final int ERROR_CUSTOMIZED = 100;

    public int mErrorType = ERROR_DATA_FORMAT;
    public int mCustomErrorType;
    public IRequest<?> mRequest;
    public Object mData;

    public static FailData unknown(IRequest<?> request) {
        return new FailData(request, ERROR_UNKNOWN, -1, null);
    }

    public static FailData inputError(IRequest<?> request) {
        return new FailData(request, ERROR_INPUT, -1, null);
    }

    public static FailData networkError(IRequest<?> request) {
        return new FailData(request, ERROR_NETWORK, -1, null);
    }

    public static FailData dataFormatError(IRequest<?> request, String content) {
        return new FailData(request, ERROR_DATA_FORMAT, -1, content);
    }

    public static FailData customizedError(IRequest<?> request, int error, Object data) {
        return new FailData(request, ERROR_CUSTOMIZED, error, data);
    }

    private FailData(IRequest<?> request, int errorType, int customErrorType, Object data) {
        mRequest = request;
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

    public IRequest<?> getRequest() {
        return mRequest;
    }

    public <T> T getData(Class<T> cls) {
        if (mData == null || !cls.isInstance(mData)) {
            return null;
        }
        return (T) mData;
    }

    public Object getRawData() {
        return mData;
    }
}
