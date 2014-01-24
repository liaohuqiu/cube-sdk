package com.srain.cube.request;

public interface RequestFinishHandler<T> {
	public void onRequestFinish(T data);
}