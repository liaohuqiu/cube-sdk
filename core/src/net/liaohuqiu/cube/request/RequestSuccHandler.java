package net.liaohuqiu.cube.request;

public interface RequestSuccHandler<T> extends RequestFinishHandler<T> {

	public T processOriginData(JsonData jsonData);
}