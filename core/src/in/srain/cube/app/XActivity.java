package in.srain.cube.app;

import in.srain.cube.app.lifecycle.IComponentContainer;
import in.srain.cube.app.lifecycle.LifeCycleComponent;
import in.srain.cube.app.lifecycle.LifeCycleComponentManager;

/**
 * 1. manager the components when move from a lifetime to another
 * 
 * @author http://www.liaohuqiu.net
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
