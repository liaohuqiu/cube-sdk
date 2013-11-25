package com.srain.sdk.request;

/**
 * 请求封装
 * 
 * @author huqiu.lhq
 */
/*
 * TODO: request parameters / request method
 */
public class Request {

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

	public String getRequestUrl() {
		return mUrl;
	}

	public void onRequestSucc(JsonData jsonData) {
		mOnSuccHandler.onRequestSucc(jsonData);
	}

	public byte[] getPostData() {
		return null;
	}
}
