package in.srain.cube.request;

/**
 * @author http://www.liaohuqiu.net
 */
public class SimpleRequest<T> extends RequestBase<T> implements IRequest<T> {

    private RequestHandler<T> mRequestHandler;

    public SimpleRequest() {

    }

    public SimpleRequest(RequestHandler<T> handler) {
        setRequestHandler(handler);
    }

    public SimpleRequest setRequestHandler(RequestHandler<T> handler) {
        mRequestHandler = handler;
        return this;
    }

    @Override
    protected void doSendRequest() {
        SimpleRequestManager.sendRequest(this);
    }

    @Override
    protected void prepareRequest() {
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
