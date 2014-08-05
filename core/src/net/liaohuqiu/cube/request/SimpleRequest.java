package net.liaohuqiu.cube.request;

/**
 * @author http://www.liaohuqiu.net
 * 
 *         TODO: request parameters / request method
 */
public class SimpleRequest<T> implements IRequest<T> {

	private RequestSuccHandler<T> mOnSuccHandler;
	private BeforeRequestHandler mBeforeRequestHandler;

	private String mUrl;

	public SimpleRequest(BeforeRequestHandler beforeRequestHandler, RequestSuccHandler<T> succHandler) {
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
	protected void doQuery() {
		SimpleRequestManager.sendRequest(this);
	}

	public void setRequestUrl(String url) {
		mUrl = url;
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
	public void onRequestSucc(T data) {
		if (null != mOnSuccHandler) {
			mOnSuccHandler.onRequestFinish(data);
		}
	}

	protected void setBeforeRequest() {
		if (null != mBeforeRequestHandler) {
			mBeforeRequestHandler.beforeRequest(this);
		}
	}

	/**
	 * Override this method to process the data from data source.
	 * 
	 * Implements interface {@link IRequest}
	 */
	@Override
	public T processOriginData(JsonData rawData) {
		if (null != mOnSuccHandler) {
			return mOnSuccHandler.processOriginData(rawData);
		}
		return null;
	}

	public byte[] getPostData() {
		return null;
	}
}
