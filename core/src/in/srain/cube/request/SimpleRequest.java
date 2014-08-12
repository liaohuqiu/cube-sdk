package in.srain.cube.request;

/**
 * @author http://www.liaohuqiu.net
 */
public class SimpleRequest<T> extends RequestBase<T> implements IRequest<T> {

    private RequestHandler<T> mRequestHandler;
    private BeforeRequestHandler mBeforeRequestHandler;

    public SimpleRequest(BeforeRequestHandler beforeRequestHandler, RequestHandler<T> handler) {
        mBeforeRequestHandler = beforeRequestHandler;
        mRequestHandler = handler;
    }

    @Override
    public void send() {
        this.beforeRequest();
        SimpleRequestManager.sendRequest(this);
    }

    @Override
    public void beforeRequest() {
        if (null != mBeforeRequestHandler) {
            mBeforeRequestHandler.beforeRequest(this);
        }
    }

    @Override
    public void onRequestSuccess(T data) {
        if (null != mRequestHandler) {
            mRequestHandler.onRequestFinish(data);
        }
    }

    @Override
    public void onRequestFail(RequestResultType requestResultType) {
        if (null != mRequestHandler) {
            mRequestHandler.onRequestFail(requestResultType);
        }
    }

    @Override
    public T processOriginDataFromServer(JsonData rawData) {
        if (null != mRequestHandler) {
            return mRequestHandler.processOriginData(rawData);
        }
        return null;
    }

    @Override
    public void cancelRequest() {

    }
}
