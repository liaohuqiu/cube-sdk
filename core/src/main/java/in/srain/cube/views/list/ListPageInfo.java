package in.srain.cube.views.list;

import java.util.ArrayList;
import java.util.List;

public class ListPageInfo<T> {

    private int mNumPerPage = 0;
    private int mStart = 0;
    private int mLastStart = 0;
    private boolean mHasMore;
    private boolean mIsBusy = false;

    private List<T> mDataList;

    public ListPageInfo(int numPerPage) {
        mNumPerPage = numPerPage;
    }

    public void updateListInfo(List<T> dataList, int total) {
        addMore(dataList);
        mHasMore = mDataList.size() < total;
        mIsBusy = false;
    }

    private void addMore(List<T> dataList) {
        if (dataList == null) {
            return;
        }
        if (mStart == 0 || mDataList == null) {
            mDataList = new ArrayList<T>();
        }
        mDataList.addAll(dataList);
    }

    public void updateListInfo(List<T> dataList, boolean hasMore) {
        addMore(dataList);
        mHasMore = hasMore;
        mIsBusy = false;
    }

    public void rollbackOnFail() {
        mIsBusy = false;
        mStart = mLastStart;
    }

    public boolean tryEnterLock() {
        if (mIsBusy) {
            return false;
        }
        mIsBusy = true;
        return true;
    }

    public int getStart() {
        return mStart;
    }

    public int getNumPerPage() {
        return mNumPerPage;
    }

    /**
     * start from 0
     *
     * @return
     */
    public int getPage() {
        return mStart / mNumPerPage;
    }

    public void goToHead() {
        mStart = 0;
    }

    public T getItem(int position) {
        if (null == mDataList || position < 0 || position > mDataList.size()) {
            return null;
        }
        return mDataList.get(position);
    }

    public boolean isEmpty() {
        return mDataList == null || mDataList.size() == 0;
    }

    /**
     * try to move to next page
     *
     * @return
     */
    public boolean prepareForNextPage() {
        if (hasMore()) {
            mLastStart = mStart;
            mStart += mNumPerPage;
            return true;
        }
        return false;
    }

    public List<T> getDataList() {
        return mDataList;
    }

    public int getListLength() {
        if (mDataList == null) {
            return 0;
        }
        return mDataList.size();
    }

    /**
     * the first item in list
     *
     * @return
     */
    public T firstItem() {
        if (mDataList == null || mDataList.size() == 0) {
            return null;
        }
        return mDataList.get(0);
    }

    /**
     * the last item in list
     *
     * @return
     */
    public T lastItem() {
        if (mDataList == null || mDataList.size() == 0) {
            return null;
        }
        return mDataList.get(mDataList.size() - 1);
    }

    public boolean hasMore() {
        return mDataList == null || mHasMore;
    }

    public boolean isFirstPage() {
        return 0 == mStart;
    }
}