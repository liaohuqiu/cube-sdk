package in.srain.cube.views.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import in.srain.cube.util.CLog;
import in.srain.cube.util.Debug;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * A adapter using View Holder to display the item of a list view;
 *
 * @param <ItemDataType>
 * @author http://www.liaohuqiu.net
 */
public abstract class ListViewDataAdapterBase<ItemDataType> extends BaseAdapter {

    private static String LOG_TAG = "cube_list";

    protected ViewHolderCreator<ItemDataType> mViewHolderCreator;
    protected ViewHolderCreator<ItemDataType> mLazyCreator;
    protected boolean mForceCreateView = false;

    public ListViewDataAdapterBase() {

    }

    public ListViewDataAdapterBase(final Object enclosingInstance, final Class<?> cls) {
        setViewHolderClass(enclosingInstance, cls);
    }

    /**
     * @param viewHolderCreator The view holder creator will create a View Holder that extends {@link ViewHolderBase}
     */
    public ListViewDataAdapterBase(ViewHolderCreator<ItemDataType> viewHolderCreator) {
        mViewHolderCreator = viewHolderCreator;
    }

    public void setViewHolderCreator(ViewHolderCreator<ItemDataType> viewHolderCreator) {
        mViewHolderCreator = viewHolderCreator;
    }

    public void setViewHolderClass(final Object enclosingInstance, final Class<?> cls, final Object... args) {
        if (cls == null) {
            throw new IllegalArgumentException("ViewHolderClass is null.");
        }

        // top class
        boolean isEnclosingInstanceClass = false;
        if (cls.getEnclosingClass() != null && !Modifier.isStatic(cls.getModifiers())) {
            isEnclosingInstanceClass = true;
        }

        // inner instance class should pass enclosing class, so +1
        int argsLen = isEnclosingInstanceClass ? args.length + 1 : args.length;

        final Object[] instanceObjects = new Object[argsLen];

        int copyStart = 0;
        // if it is inner instance class, first argument should be the enclosing class instance
        if (isEnclosingInstanceClass) {
            instanceObjects[0] = enclosingInstance;
            copyStart = 1;
        }

        // has copy construction parameters
        if (args.length > 0) {
            System.arraycopy(args, 0, instanceObjects, copyStart, args.length);
        }

        // fill the types
        final Class[] parameterTypes = new Class[argsLen];
        for (int i = 0; i < instanceObjects.length; i++) {
            parameterTypes[i] = instanceObjects[i].getClass();
        }

        Constructor<?> constructor = null;
        try {
            constructor = cls.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        if (constructor == null) {
            throw new IllegalArgumentException("ViewHolderClass can not be initiated");
        }

        final Constructor<?> finalConstructor = constructor;

        mLazyCreator = new ViewHolderCreator<ItemDataType>() {
            @Override
            public ViewHolderBase<ItemDataType> createViewHolder() {
                Object object = null;
                try {
                    boolean isAccessible = finalConstructor.isAccessible();
                    if (!isAccessible) {
                        finalConstructor.setAccessible(true);
                    }
                    object = finalConstructor.newInstance(instanceObjects);
                    if (!isAccessible) {
                        finalConstructor.setAccessible(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (object == null || !(object instanceof ViewHolderBase)) {
                    throw new IllegalArgumentException("ViewHolderClass can not be initiated");
                }
                return (ViewHolderBase<ItemDataType>) object;
            }
        };
    }

    private ViewHolderBase<ItemDataType> createViewHolder() {
        if (mViewHolderCreator == null && mLazyCreator == null) {
            throw new RuntimeException("view holder creator is null");
        }
        if (mViewHolderCreator != null) {
            return mViewHolderCreator.createViewHolder();
        }
        if (mLazyCreator != null) {
            return mLazyCreator.createViewHolder();
        }
        return null;
    }

    public void forceCreateView(boolean yes) {
        mForceCreateView = yes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (Debug.DEBUG_LIST) {
            CLog.d(LOG_TAG, "getView %s", position);
        }
        ItemDataType itemData = getItem(position);
        ViewHolderBase<ItemDataType> holderBase = null;
        if (mForceCreateView || convertView == null || (!(convertView.getTag() instanceof ViewHolderBase<?>))) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            holderBase = createViewHolder();
            if (holderBase != null) {
                convertView = holderBase.createView(inflater);
                if (convertView != null) {
                    if (!mForceCreateView) {
                        convertView.setTag(holderBase);
                    }
                }
            }
        } else {
            holderBase = (ViewHolderBase<ItemDataType>) convertView.getTag();
        }
        if (holderBase != null) {
            holderBase.setItemData(position, convertView);
            holderBase.showData(position, itemData);
        }
        return convertView;
    }

    @Override
    public abstract ItemDataType getItem(int position);
}
