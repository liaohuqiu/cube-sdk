package net.liaohuqiu.cube.app;

public interface ICubeFragement {

	void onComeIn(Object data);

	void onLeave();

	void onBack(Object data);

	boolean stayWhenBackPressed();
}