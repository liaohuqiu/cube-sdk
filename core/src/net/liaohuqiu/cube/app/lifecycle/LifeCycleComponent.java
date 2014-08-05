package net.liaohuqiu.cube.app.lifecycle;

public interface LifeCycleComponent {

	public void onRestart();

	public void onPause();

	public void onResume();

	public void onStop();

	public void onDestroy();
}
