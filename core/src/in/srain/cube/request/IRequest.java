package in.srain.cube.request;

public interface IRequest<T> {

    public RequestData getRequestData();

    public void onRequestSuccess(T data);

    public void onRequestFail(FailData failData);

    /**
     * send request
     */
    public void send();

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