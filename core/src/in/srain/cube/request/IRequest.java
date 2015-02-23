package in.srain.cube.request;

public interface IRequest<T> {

    public RequestData getRequestData();

    public void onRequestSuccess(T data);

    public void onRequestFail(FailData failData);

    public RequestBase setFailData(FailData failData);

    public FailData getFailData();

    /**
     * send request
     */
    public void send();

    /**
     * request synchronously
     */
    public T requestSync();

    public void cancelRequest();

    public T onDataFromServer(String data);

    /**
     * filter the origin data or convert its structure.
     *
     * @param jsonData
     * @return
     */
    T processOriginDataFromServer(JsonData jsonData);
}