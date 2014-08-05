package in.srain.cube.request;

public abstract class JsonCacheableRequestSuccHandler implements CacheableRequestSuccHandler<JsonData> {

	public abstract void onCacheData(JsonData data, boolean outoufDate);

	@Override
	public JsonData processOriginData(JsonData jsonData) {
		return jsonData;

	}
}