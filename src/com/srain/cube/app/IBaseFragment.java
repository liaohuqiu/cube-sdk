package com.srain.cube.app;

public interface IBaseFragment {

	void onComeIn(Object data);

	void onLeave();

	void onBack(Object data);

	boolean stayWhenBackPressed();
}