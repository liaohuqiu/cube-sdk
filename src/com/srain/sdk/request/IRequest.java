package com.srain.sdk.request;

public interface IRequest {

	public String getRequestUrl();

	public void onRequestSucc(JsonData jsonData);

	/**
	 * filter the origin data or convert its structure.
	 * 
	 * @param jsonData
	 * @return
	 */
	JsonData processOriginData(JsonData jsonData);
}
