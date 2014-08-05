package net.liaohuqiu.cube.request;

public interface IRequest<T> {

	public String getRequestUrl();

	public void onRequestSucc(T data);

	/**
	 * filter the origin data or convert its structure.
	 * 
	 * @param jsonData
	 * @return
	 */
	T processOriginData(JsonData jsonData);
}