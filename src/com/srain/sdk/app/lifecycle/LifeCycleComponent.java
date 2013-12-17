package com.srain.sdk.app.lifecycle;

public interface LifeCycleComponent {
	public void onResume();

	public void onStop();

	public void onDestroy();
}
