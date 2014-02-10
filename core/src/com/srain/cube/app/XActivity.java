package com.srain.cube.app;

import com.srain.cube.app.lifecycle.IComponentContainer;
import com.srain.cube.app.lifecycle.LifeCycleComponent;
import com.srain.cube.app.lifecycle.LifeCycleComponentManager;

/**
 * 1. manager the components when move from a lifetime to another
 * 
 * @author huqiu.lhq
 */
public abstract class XActivity extends CubeFragmentActivity implements IComponentContainer {

	private LifeCycleComponentManager mComponentContainer = new LifeCycleComponentManager();

	@Override
	protected void onRestart() {
		super.onStart();
		mComponentContainer.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mComponentContainer.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mComponentContainer.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mComponentContainer.onDestroy();
	}

	@Override
	public void addComponent(LifeCycleComponent component) {
		mComponentContainer.addComponent(component);
	}
}
