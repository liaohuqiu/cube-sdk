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

    public void setViewHolderClass(final Object enclosingInstance, final Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("ViewHolderClass is null.");
        }
        mLazyCreator = new ViewHolderCreator<ItemDataType>() {
            @Override
            public ViewHolderBase<ItemDataType> createViewHolder() {
                Object object = null;
                try {
                    // top class
                    if (cls.getEnclosingClass() == null) {
                        Constructor<?> constructor = cls.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        object = constructor.newInstance();
                    } else {
                        if (Modifier.isStatic(cls.getModifiers())) {
                            Constructor<?> constructor = cls.getDeclaredConstructor();
                            constructor.setAccessible(true);
                            object = constructor.newInstance();
                        } else {
                            Constructor<?> constructor = cls.getDeclaredConstructor(enclosingInstance.getClass());
                            constructor.setAccessible(true);
                            object = constructor.newInstance(enclosingInstance);
                        }
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
