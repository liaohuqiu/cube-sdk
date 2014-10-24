package in.srain.cube.views.list;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import in.srain.cube.util.CLog;

import java.util.HashSet;

/**
 * A adapter using View Holder to display the item of a list view;
 *
 * @param <ItemDataType>
 * @author http://www.liaohuqiu.net
 */
public abstract class ListViewDataAdapterBase<ItemDataType> extends BaseAdapter {

    private static String LOG_TAG = "cube_list";

    protected ViewHolderCreator<ItemDataType> mViewHolderCreator;
    protected boolean mForceCreateView = false;

    public ListViewDataAdapterBase() {

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

    public void forceCreateView(boolean yes) {
        mForceCreateView = yes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mViewHolderCreator == null) {
            throw new RuntimeException("view holder creator is null");
        }
        if (CLog.DEBUG_LIST) {
            Log.d(LOG_TAG, String.format("getView %s", position));
        }
        ItemDataType itemData = getItem(position);
        ViewHolderBase<ItemDataType> holderBase = null;
        if (mForceCreateView || convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            holderBase = mViewHolderCreator.createViewHolder();
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
            holderBase.setItemData(position);
            holderBase.showData(position, itemData);
        }
        return convertView;
    }

    @Override
    public abstract ItemDataType getItem(int position);
}
