package in.srain.cube.request;

/**
 * @author http://www.liaohuqiu.net
 */
public class SimpleJsonRequest extends SimpleRequest<JsonData> {

    public SimpleJsonRequest(RequestPreHandler requestPreHandler, RequestJsonHandler succHandler) {
        super(requestPreHandler, succHandler);
    }
}