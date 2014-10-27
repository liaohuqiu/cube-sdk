package in.srain.cube.request;

public abstract class RequestBase<T> implements IRequest<T> {

    private RequestData mRequestData = new RequestData();
    private boolean mHasBeenCanceled = false;

    public RequestData getRequestData() {
        return mRequestData;
    }

    @Override
    public void send() {
        prepareRequest();
        doSendRequest();
    }

    @Override
    public void cancelRequest() {
        mHasBeenCanceled = true;
        onCancel();
    }

    @Override
    public T onDataFromServer(String data) {
        JsonData jsonData = JsonData.create(data);
        return processOriginDataFromServer(jsonData);
    }

    protected boolean hasBeenCanceled() {
        return mHasBeenCanceled;
    }

    protected void onCancel() {

    }

    /**
     * implement this method to process request data
     */
    protected abstract void doSendRequest();

    /**
     * prepare request
     */
    protected abstract void prepareRequest();
}
