package com.srain.cube.app;

import android.support.v4.app.Fragment;

public abstract class CubeFragment extends Fragment implements ICubeFragement {

	protected Object mDataIn;

	public CubeFragmentActivity getContext() {
		return (CubeFragmentActivity) getActivity();
	}

	// ===========================================================
	// implements IBaseFragment
	// ===========================================================
	@Override
	public void onComeIn(Object data) {
		mDataIn = data;
	}

	@Override
	public void onLeave() {

	}

	@Override
	public void onBack(Object data) {

	}

	@Override
	public boolean stayWhenBackPressed() {
		return false;
	}
}
