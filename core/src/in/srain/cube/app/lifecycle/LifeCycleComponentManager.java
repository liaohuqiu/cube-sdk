package in.srain.cube.app.lifecycle;

import android.content.Context;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class LifeCycleComponentManager implements IComponentContainer {

    private HashMap<String, LifeCycleComponent> mComponentList;

    public static void tryAddComponentToContainer(LifeCycleComponent component, Context context) {
        if (context instanceof IComponentContainer) {
            ((IComponentContainer) context).addComponent(component);
        } else {
            throw new IllegalArgumentException("componentContainerContext should implements IComponentContainer");
        }
    }

    public LifeCycleComponentManager() {
    }

    public void addComponent(LifeCycleComponent component) {
        if (component != null) {
            getList().put(component.toString(), component);
        }
    }

    private HashMap<String, LifeCycleComponent> getList() {
        if (mComponentList == null) {
            mComponentList = new HashMap<String, LifeCycleComponent>();
        }
        return mComponentList;
    }

    public void onRestart() {

        if (mComponentList == null) {
            return;
        }

        Iterator<Entry<String, LifeCycleComponent>> it = getIterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onRestart();
            }
        }
    }

    private Iterator<Entry<String, LifeCycleComponent>> getIterator() {
        Iterator<Entry<String, LifeCycleComponent>> it = mComponentList.entrySet().iterator();
        return it;
    }

    public void onStop() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = getIterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onStop();
            }
        }
    }

    public void onPause() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = getIterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onPause();
            }
        }
    }

    public void onResume() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = getIterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onResume();
            }
        }
    }

    public void onDestroy() {
        if (mComponentList == null) {
            return;
        }
        Iterator<Entry<String, LifeCycleComponent>> it = getIterator();
        while (it.hasNext()) {
            LifeCycleComponent component = it.next().getValue();
            if (component != null) {
                component.onDestroy();
            }
        }
    }
}
