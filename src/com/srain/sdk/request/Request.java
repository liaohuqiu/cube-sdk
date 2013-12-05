package com.srain.sdk.request;

/**
 * 请求封装
 * 
 * @author huqiu.lhq
 */
/*
 * TODO: request parameters / request method
 */
public class Request implements IRequest {

	private RequestOnSuccHandler mOnSuccHandler;
	private RequestPreHandler mBeforeRequestHandler;

	private String mUrl;

	public Request(RequestPreHandler beforeRequestHandler, RequestOnSuccHandler succHandler) {
		mBeforeRequestHandler = beforeRequestHandler;
		mOnSuccHandler = succHandler;
	}

	public Request send() {
		prepare();
		doQuery();
		return this;
	}

	protected void doQuery() {
		RequestManager.sendRequest(this);
	}

	public void prepare() {
		mBeforeRequestHandler.beforeRequest(this);
	}

	public Request setRequestUrl(String url) {
		mUrl = url;
		return this;
	}

	/**
	 * Implements interface {@link IRequest}
	 */
	public String getRequestUrl() {
		return mUrl;
	}

	/**
	 * Implements interface {@link IRequest}
	 */
	public void onRequestSucc(JsonData jsonData) {
		sendRequestSucc(jsonData);
	}

	protected void sendRequestSucc(JsonData jsonData) {
		mOnSuccHandler.onRequestSucc(jsonData);
	}

	/**
	 * Override this method to process the data from data srouce.
	 */
	public JsonData processOriginData(JsonData rawData) {
		return rawData;
	}

	public byte[] getPostData() {
		return null;
	}
}
