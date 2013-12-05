package com.srain.sdk.request;

/**
 * 
 * Define what should be done before send a cacheable request.
 * 
 * @author huqiu.lhq
 * 
 */
public interface RequestPreHandler {
	/**
	 * Prepare the request: add parameters, build up the request data.
	 */
	public void beforeRequest(Request request);
}
