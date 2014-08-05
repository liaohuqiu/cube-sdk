package net.liaohuqiu.cube.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class CubeFragment extends Fragment implements ICubeFragement {

	protected Object mDataIn;

	protected abstract View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	public CubeFragmentActivity getContext() {
		return (CubeFragmentActivity) getActivity();
	}

	// ===========================================================
	// implements IBaseFragment
	// ===========================================================
	@Override
	public void onComeIn(Object data) {
		mDataIn = data;
		showStatus("onComeIn");
	}

	@Override
	public void onLeave() {
		showStatus("onLeave");
	}

	@Override
	public void onBack(Object data) {
		showStatus("onBack");
	}

	@Override
	public void onStop() {
		super.onStop();
		showStatus("onStop");
	}

	@Override
	public void onResume() {
		super.onResume();
		showStatus("onResume");
	}

	@Override
	public boolean stayWhenBackPressed() {
		return false;
	}

	private void showStatus(String status) {
		Log.d("test", String.format("%s %s", this.getClass().getName(), status));
	}
}
