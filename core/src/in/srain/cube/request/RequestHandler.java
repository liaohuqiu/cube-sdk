package in.srain.cube.request;

public interface RequestHandler<T> extends RequestFinishHandler<T> {

    public T processOriginData(JsonData jsonData);

    public void onRequestFail(RequestResultType requestResultType);
}