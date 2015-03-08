package in.srain.cube.request;

public class RequestManager {

    private static RequestManager sInstance;
    private IRequestProxy mProxy;

    public static RequestManager getInstance() {
        if (sInstance == null) {
            sInstance = new RequestManager();
        }
        return sInstance;
    }

    public void setRequestProxy(IRequestProxy proxy) {
        mProxy = proxy;
    }

    public IRequestProxy getRequestProxy() {
        if (mProxy == null) {
            return DefaultRequestProxy.getInstance();
        }
        return mProxy;
    }
}
