package in.srain.cube.cache;

import in.srain.cube.request.JsonData;

public abstract class QueryJsonHandler implements QueryHandler<JsonData> {

    @Override
    public JsonData processRawDataFromCache(JsonData rawData) {
        return rawData;
    }
}
