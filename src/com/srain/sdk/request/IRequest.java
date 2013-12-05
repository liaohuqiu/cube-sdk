package com.srain.sdk.request;

/**
 * Describe the common actions of a request.
 * 
 * @author huqiu.lhq
 */
public interface IRequest {

	/**
	 * detective the url
	 */
	public String getRequestUrl();

	/**
	 * 
	 */
	public void onRequestSucc(JsonData jsonData);

	/**
	 * Filter the origin data or convert its structure.
	 */
	JsonData processOriginData(JsonData jsonData);
}
