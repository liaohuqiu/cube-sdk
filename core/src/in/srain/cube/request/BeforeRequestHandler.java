package in.srain.cube.request;

public interface BeforeRequestHandler {
    public <T> void beforeRequest(RequestBase<T> request);
}