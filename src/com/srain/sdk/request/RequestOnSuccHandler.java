package com.srain.sdk.request;

/**
 * Define the things you can do after a request is accomlished.
 */
public interface RequestOnSuccHandler {
	/**
	 * After request is accomlished.
	 */
	public void onRequestSucc(JsonData jsonData);
}