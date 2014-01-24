package com.srain.cube.app.lifecycle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.content.Context;

public class LifeCycleComponentManager implements IComponentContainer {

	private HashMap<String, WeakReference<LifeCycleComponent>> mComponentList;

	public static void tryAddComponentToContainer(LifeCycleComponent component, Context componentContainerContext) {
		if (componentContainerContext instanceof IComponentContainer) {
			((IComponentContainer) componentContainerContext).addComponent(component);
		} else {
			throw new IllegalArgumentException("componentContainerContext should impletemnts IComponentContainer");
		}
	}

	public LifeCycleComponentManager() {
		mComponentList = new HashMap<String, WeakReference<LifeCycleComponent>>();
	}

	public void addComponent(LifeCycleComponent component) {
		if (component != null) {
			mComponentList.put(component.toString(), new WeakReference<LifeCycleComponent>(component));
		}
	}

	public void onRestart() {

		for (Iterator<Entry<String, WeakReference<LifeCycleComponent>>> it = mComponentList.entrySet().iterator(); it.hasNext();) {
			LifeCycleComponent component = it.next().getValue().get();
			if (null != component) {
				component.onRestart();
			} else {
				it.remove();
			}
		}
	}

	public void onStop() {
		for (Iterator<Entry<String, WeakReference<LifeCycleComponent>>> it = mComponentList.entrySet().iterator(); it.hasNext();) {
			LifeCycleComponent component = it.next().getValue().get();
			if (null != component) {
				component.onStop();
			} else {
				it.remove();
			}
		}
	}

	public void onPause() {
		for (Iterator<Entry<String, WeakReference<LifeCycleComponent>>> it = mComponentList.entrySet().iterator(); it.hasNext();) {
			LifeCycleComponent component = it.next().getValue().get();
			if (null != component) {
				component.onPause();
			} else {
				it.remove();
			}
		}
	}

	public void onResume() {
		for (Iterator<Entry<String, WeakReference<LifeCycleComponent>>> it = mComponentList.entrySet().iterator(); it.hasNext();) {
			LifeCycleComponent component = it.next().getValue().get();
			if (null != component) {
				component.onResume();
			} else {
				it.remove();
			}
		}
	}

	public void onDestroy() {
		for (Iterator<Entry<String, WeakReference<LifeCycleComponent>>> it = mComponentList.entrySet().iterator(); it.hasNext();) {
			LifeCycleComponent component = it.next().getValue().get();
			if (null != component) {
				component.onDestroy();
			} else {
				it.remove();
			}
		}
	}
}
