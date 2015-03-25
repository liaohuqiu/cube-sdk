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
        RequestManager.getInstance().getRequestProxy().sendRequest(this);
    }

    @Override
    protected T doRequestSync() {
        return RequestManager.getInstance().getRequestProxy().requestSync(this);
    }

    @Override
    protected void prepareRequest() {
        RequestManager.getInstance().getRequestProxy().sendRequest(this);
    }

    @Override
    public void onRequestSuccess(T data) {
        if (null != mRequestHandler) {
            mRequestHandler.onRequestFinish(data);
        }
    }

    @Override
    public void onRequestFail(FailData failData) {
        RequestManager.getInstance().getRequestProxy().onRequestFail(this, failData);
        if (null != mRequestHandler) {
            mRequestHandler.onRequestFail(failData);
        }
    }

    @Override
    public T processOriginDataFromServer(JsonData rawData) {
        rawData = RequestManager.getInstance().getRequestProxy().processOriginDataFromServer(this, rawData);
        if (null != mRequestHandler) {
            return mRequestHandler.processOriginData(rawData);
        }
        return null;
    }
}
