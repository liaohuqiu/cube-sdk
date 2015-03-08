package in.srain.cube.request;

/**
 * Created by srain on 3/8/15.
 */
public interface IRequestProxy {

    <T> T requestSync(IRequest<T> request);

    <T> void sendRequest(IRequest<T> request);
}
