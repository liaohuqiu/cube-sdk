package com.srain.cube.app;

import android.app.FragmentTransaction;

public class FragmentParameter {

	enum TYPE {
		ADD, REPLACE
	};

	public BaseFragment from;
	public Class<?> cls;
	public Object data;
	public int transition = FragmentTransaction.TRANSIT_UNSET;
	public int containerId;
	public TYPE type = TYPE.ADD;
	public boolean addToBackStack = true;
}