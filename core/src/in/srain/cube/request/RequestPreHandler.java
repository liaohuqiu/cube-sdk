package in.srain.cube.request;

public interface RequestPreHandler {
    public <T> void prepareRequest(RequestBase<T> request);
}