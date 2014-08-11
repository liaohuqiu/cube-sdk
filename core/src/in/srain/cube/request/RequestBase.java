package in.srain.cube.request;

public abstract class RequestBase<T> implements IRequest<T> {

    private RequestData mRequestData = new RequestData();

    public RequestData getRequestData() {
        return mRequestData;
    }
}
