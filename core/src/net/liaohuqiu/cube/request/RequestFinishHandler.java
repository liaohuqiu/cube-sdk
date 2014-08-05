package net.liaohuqiu.cube.request;

public interface RequestFinishHandler<T> {
	public void onRequestFinish(T data);
}