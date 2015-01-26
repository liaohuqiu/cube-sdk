package in.srain.cube.views.block;

import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

public abstract class BlockListAdapter<T> {

    private List<T> mItemList;
    private BlockListView mView;

    // default size is wrap_content
    private int mBlockWidth = -2;
    private int mBlockHeight = -2;

    private int mWidthSpace = 0;
    private int mHeightSpace = 0;

    private int mColumnNum = 0;

    public BlockListAdapter() {
    }

    public T getItem(int position) {
        return mItemList.get(position);
    }

    public void registerView(BlockListView observer) {
        mView = observer;
    }

    public void displayBlocks(List<T> itemList) {
        if (null == itemList) {
            return;
        }
        mItemList = itemList;

        if (null == mView) {
            throw new IllegalArgumentException("Adapter has not been attached to any BlockListView");
        }
        mView.onDataListChange();
    }

    public abstract View getView(LayoutInflater layoutInflater, int position);

    public int getCount() {
        return mItemList.size();
    }

    public void setSpace(int w, int h) {
        mWidthSpace = w;
        mHeightSpace = h;
    }

    public int getHorizontalSpacing() {
        return mWidthSpace;
    }

    public int getVerticalSpacing() {
        return mHeightSpace;
    }

    public void setBlockSize(int w, int h) {
        mBlockWidth = w;
        mBlockHeight = h;
    }

    public int getBlockWidth() {
        return mBlockWidth;
    }

    public int getBlockHeight() {
        return mBlockHeight;
    }

    public void setColumnNum(int num) {
        mColumnNum = num;
    }

    public int getCloumnNum() {
        return mColumnNum;
    }
}