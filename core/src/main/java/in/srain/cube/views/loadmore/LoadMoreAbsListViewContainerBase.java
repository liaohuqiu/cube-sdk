package in.srain.cube.views.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;

/**
 * @author huqiu.lhq
 */
public abstract class LoadMoreAbsListViewContainerBase extends LoadMoreContainerBase {

    private AbsListView mAbsListView;
    protected AbsListView.OnScrollListener mOnScrollListener;

    public LoadMoreAbsListViewContainerBase(Context context) {
        super(context);
    }

    public LoadMoreAbsListViewContainerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mAbsListView = retrieveAbsListView();
        mAbsListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private boolean mIsEnd = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (null != mOnScrollListener) {
                    mOnScrollListener.onScrollStateChanged(view, scrollState);
                }
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (mIsEnd) {
                        onReachBottom();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (null != mOnScrollListener) {
                    mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
                if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
                    mIsEnd = true;
                } else {
                    mIsEnd = false;
                }
            }
        });
    }

    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    protected Object retrieveListView() {
        return retrieveAbsListView();
    }

    protected abstract AbsListView retrieveAbsListView();
}