package in.srain.cube.app.lifecycle;

import android.content.Context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class LifeCycleComponentManager implements IComponentContainer {

    private HashMap<String, LifeCycleComponent> mComponentList;

    public LifeCycleComponentManager() {
    }

    public static void tryAddComponentToContainer(LifeCycleComponent component, Context context) {
        if (context instanceof IComponentContainer) {
            ((IComponentContainer) context).addComponent(component);
        } else {
            throw new IllegalArgumentException("componentContainerContext should implements IComponentContainer");
        }
    }

    public void addComponent(LifeCycleComponent component) {
        if (component != null) {
            if (mComponentList == null) {
                mComponentList = new HashMap<String, LifeCycleComponent>();
            }
            mComponentList.put(component.toString(), component);
        }
    }

    public void onBecomesVisibleFromTotallyInvisible() {

        if (mComponentList == null) {
            return;
        }

        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onBecomesVisibleFromTotallyInvisible();
            }
        }
    }

    public void onBecomesTotallyInvisible() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onBecomesTotallyInvisible();
            }
        }
    }

    public void onBecomesPartiallyInvisible() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onBecomesPartiallyInvisible();
            }
        }
    }

    public void onBecomesVisibleFromPartiallyInvisible() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onBecomesVisible();
            }
        }
    }

    public void onDestroy() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.entrySet().iterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onDestroy();
            }
        }
    }
}
