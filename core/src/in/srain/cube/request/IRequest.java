package in.srain.cube.request;

public interface IRequest<T> {

    public RequestData getRequestData();

    public void onRequestSuccess(T data);

    public void beforeRequest();

    public void onRequestFail(RequestResultType requestResultType);

    public void send();

    public void cancelRequest();

    /**
     * filter the origin data or convert its structure.
     *
     * @param jsonData
     * @return
     */
    T processOriginDataFromServer(JsonData jsonData);
}