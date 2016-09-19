package in.srain.cube.request;

public abstract class RequestBase<T> implements IRequest<T> {

    private RequestData mRequestData = new RequestData();
    private FailData mFailData;
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
    public T requestSync() {
        prepareRequest();
        return doRequestSync();
    }

    @Override
    public void cancelRequest() {
        mHasBeenCanceled = true;
        onCancel();
    }

    @Override
    public T onDataFromServer(String data) {
        JsonData jsonData = JsonData.create(data);
        if (jsonData == null || jsonData.length() == 0) {
            setFailData(FailData.dataFormatError(this, data));
            return null;
        }
        return processOriginDataFromServer(jsonData);
    }

    protected boolean hasBeenCanceled() {
        return mHasBeenCanceled;
    }

    protected void onCancel() {

    }

    @Override
    public RequestBase setFailData(FailData failData) {
        mFailData = failData;
        return this;
    }

    @Override
    public FailData getFailData() {
        return mFailData;
    }

    /**
     * implement this method to process request data
     */
    protected abstract void doSendRequest();

    /**
     * implement this method to request data synchronously
     */
    protected abstract T doRequestSync();

    /**
     * prepare request
     */
    protected abstract void prepareRequest();
}
