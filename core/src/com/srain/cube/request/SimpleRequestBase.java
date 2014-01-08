package com.srain.cube.request;

/**
 * @author huqiu.lhq
 * 
 *         TODO: request parameters / request method
 */
public abstract class SimpleRequestBase implements IRequest<JsonData> {

	private RequestOnSuccHandler mOnSuccHandler;
	private RequestPreHandler mBeforeRequestHandler;

	private String mUrl;

	public SimpleRequestBase(RequestPreHandler beforeRequestHandler, RequestOnSuccHandler succHandler) {
		mBeforeRequestHandler = beforeRequestHandler;
		mOnSuccHandler = succHandler;
	}

	public void send() {
		setBeforeRequest();
		doQuery();
	}

	/**
	 * Use a request Manger to send this SimpleRequestBase
	 */
	public abstract void doQuery();

	public SimpleRequestBase setRequestUrl(String url) {
		mUrl = url;
		return this;
	}

	/**
	 * Implements interface {@link IRequest}
	 */
	@Override
	public String getRequestUrl() {
		return mUrl;
	}

	/**
	 * Implements interface {@link IRequest}
	 */
	@Override
	public void onRequestSucc(JsonData jsonData) {
		sendRequestSucc(jsonData);
	}

	protected void sendRequestSucc(JsonData jsonData) {
		if (null != mOnSuccHandler) {
			mOnSuccHandler.onRequestSucc(jsonData);
		}
	}

	protected void setBeforeRequest() {
		if (null != mBeforeRequestHandler) {
			mBeforeRequestHandler.beforeRequest(this);
		}
	}

	/**
	 * Override this method to process the data from data srouce.
	 * 
	 * Implements interface {@link IRequest}
	 */
	@Override
	public JsonData processOriginData(JsonData rawData) {
		return rawData;
	}

	public byte[] getPostData() {
		return null;
	}
}
