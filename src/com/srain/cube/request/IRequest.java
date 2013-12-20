package com.srain.cube.request;

/**
 * Describe the common actions of a request.
 * 
 * @author huqiu.lhq
 */
public interface IRequest<T> {

	/**
	 * detective the url
	 */
	public String getRequestUrl();

	/**
	 * 
	 */
	public void onRequestSucc(T jsonData);

	/**
	 * Filter the origin data or convert its structure.
	 */
	JsonData processOriginData(JsonData jsonData);
}
