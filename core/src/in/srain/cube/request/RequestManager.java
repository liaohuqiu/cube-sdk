package in.srain.cube.request;

public class RequestManager {

    private static RequestManager sInstance;
    private RequestProxyFactory mFactory;

    public static RequestManager getInstance() {
        if (sInstance == null) {
            sInstance = new RequestManager();
        }
        return sInstance;
    }

    public void setRequestProxyFactory(RequestProxyFactory factory) {
        mFactory = factory;
    }

    public IRequestProxy getRequestProxy(IRequest request) {
        if (mFactory != null) {
            IRequestProxy proxy = mFactory.createProxyForRequest(request);
            if (proxy != null) {
                return proxy;
            }
        }
        return DefaultRequestProxy.getInstance();
    }
}
