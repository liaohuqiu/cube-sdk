package com.srain.sdk.app.lifecycle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class LifeCycleComponentManager implements IComponentContainer {

	private List<WeakReference<LifeCycleComponent>> mComponentList;

	public static void tryAddComponentToContainer(LifeCycleComponent component, Context componentContainerContext) {
		if (componentContainerContext instanceof IComponentContainer) {
			((IComponentContainer) componentContainerContext).addComponent(component);
		} else {

		}
	}

	public LifeCycleComponentManager() {
		mComponentList = new ArrayList<WeakReference<LifeCycleComponent>>();
	}

	public void addComponent(LifeCycleComponent component) {
		mComponentList.add(new WeakReference<LifeCycleComponent>(component));
	}

	public void onStop() {
		for (int i = 0; i < mComponentList.size(); i++) {
			LifeCycleComponent component = mComponentList.get(i).get();
			if (null != component) {
				component.onStop();
			}
		}
	}

	public void onResume() {
		for (int i = 0; i < mComponentList.size(); i++) {
			LifeCycleComponent component = mComponentList.get(i).get();
			if (null != component) {
				component.onResume();
			}
		}
	}

	public void onDestroy() {
		for (int i = 0; i < mComponentList.size(); i++) {
			LifeCycleComponent component = mComponentList.get(i).get();
			if (null != component) {
				component.onDestroy();
			}
		}
	}
}
