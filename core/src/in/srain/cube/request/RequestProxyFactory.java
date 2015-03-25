package in.srain.cube.request;

public interface RequestProxyFactory {
    IRequestProxy createProxyForRequest(IRequest request);
}
