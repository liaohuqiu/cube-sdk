package com.loopj.android.http;

public interface RequestProgressHandler {

	public void updateProgress(long position, long total);
}
