package in.srain.cube.request;

public interface IRequestProxy {

    <T> T requestSync(IRequest<T> request);

    <T> void sendRequest(IRequest<T> request);

    public void prepareRequest(RequestBase request);

    public void onRequestFail(RequestBase request, FailData failData);

    public JsonData processOriginDataFromServer(RequestBase request, final JsonData data);
}
