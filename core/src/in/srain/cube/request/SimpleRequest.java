package in.srain.cube.request;

/**
 * @author http://www.liaohuqiu.net
 */
public class SimpleRequest<T> extends RequestBase<T> implements IRequest<T> {

    private RequestHandler<T> mRequestHandler;
    private RequestPreHandler mRequestPreHandler;

    public SimpleRequest(RequestPreHandler requestPreHandler, RequestHandler<T> handler) {
        mRequestPreHandler = requestPreHandler;
        mRequestHandler = handler;
    }

    @Override
    protected void doSendRequest() {
        SimpleRequestManager.sendRequest(this);
    }

    @Override
    public void prepareRequest() {
        if (null != mRequestPreHandler) {
            mRequestPreHandler.prepareRequest(this);
        }
    }

    @Override
    public void onRequestSuccess(T data) {
        if (null != mRequestHandler) {
            mRequestHandler.onRequestFinish(data);
        }
    }

    @Override
    public void onRequestFail(FailData failData) {
        if (null != mRequestHandler) {
            mRequestHandler.onRequestFail(failData);
        }
    }

    @Override
    public T processOriginDataFromServer(JsonData rawData) {
        if (null != mRequestHandler) {
            return mRequestHandler.processOriginData(rawData);
        }
        return null;
    }
}
