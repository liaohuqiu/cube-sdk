package net.liaohuqiu.cube.request;

public abstract class JsonRequestSuccHandler implements RequestSuccHandler<JsonData> {
	@Override
	public JsonData processOriginData(JsonData jsonData) {
		return jsonData;
	}
}