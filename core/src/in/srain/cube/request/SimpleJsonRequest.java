package in.srain.cube.request;

/**
 * @author http://www.liaohuqiu.net
 */
public class SimpleJsonRequest extends SimpleRequest<JsonData> {

    public SimpleJsonRequest(BeforeRequestHandler beforeRequestHandler, RequestJsonHandler succHandler) {
        super(beforeRequestHandler, succHandler);
    }
}