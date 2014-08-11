package in.srain.cube.request;

public abstract class RequestJsonHandler extends RequestDefaultHandler<JsonData> {

    @Override
    public JsonData processOriginData(JsonData jsonData) {
        return jsonData;
    }
}