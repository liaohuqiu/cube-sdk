package in.srain.cube.views.list;

import android.view.LayoutInflater;
import android.view.View;

/**
 * As described in
 * <p/>
 * <a href="http://developer.android.com/training/improving-layouts/smooth-scrolling.html">http://developer.android.com/training/improving-layouts/smooth-scrolling.html</a>
 * <p/>
 * Using A View Holder in Listview getView() method is a good practice in using listview;
 * <p/>
 * This class encapsulate the base operate of a View Holder: createView / showData
 *
 * @param <ItemDataType> the generic type of the data in each item
 * @author http://www.liaohuqiu.net
 */
public abstract class ViewHolderBase<ItemDataType> {

    protected int mLastPosition;
    protected int mPosition = -1;

    /**
     * create a view from resource Xml file, and hold the view that may be used in displaying data.
     */
    public abstract View createView(LayoutInflater layoutInflater);

    /**
     * using the holed views to display data
     */
    public abstract void showData(int position, ItemDataType itemData);

    public void setItemData(int position) {

        mLastPosition = mPosition;
        mPosition = position;
    }

    /**
     * Check if the View Holder is still display the same data after back to screen.
     * <p/>
     * A view in a listview or gridview may go down the screen and then back,
     * <p/>
     * for efficiency, in getView() method, a convertView will be reused.
     * <p/>
     * If the convertView is reused, View Holder will hold new data.
     */
    public boolean stillHoldLastItemData() {
        return mLastPosition == mPosition;
    }
}