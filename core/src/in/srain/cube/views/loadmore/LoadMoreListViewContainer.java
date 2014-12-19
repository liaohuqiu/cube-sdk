package in.srain.cube.views.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import in.srain.cube.util.CLog;

/**
 * @author huqiu.lhq
 */
public class LoadMoreListViewContainer extends LinearLayout implements LoadMoreContainer {

    private AbsListView.OnScrollListener mOnScrollListener;
    private LoadMoreUIHandler mLoadMoreUIHandler;
    private LoadMoreHandler mLoadMoreHandler;

    private boolean mIsLoading;
    private boolean mHasMore = true;
    private boolean mAutoLoadMore = true;
    private boolean mShowLoadingForFirstPage = true;
    private View mFooterView;

    private ListView mListView;
    private int mPage = 0;

    public LoadMoreListViewContainer(Context context) {
        super(context);
    }

    public LoadMoreListViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = getChildAt(0);
        mListView = (ListView) view;
        init();
    }

    public void useDefaultHeader() {
        LoadMoreDefaultFooterView footerView = new LoadMoreDefaultFooterView(getContext());
        setLoadMoreView(footerView);
        setLoadMoreUIHandler(footerView);
    }

    private void init() {

        if (mFooterView != null) {
            mListView.addFooterView(mFooterView);
        }

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

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

    private void performLoadMore() {
        if (mIsLoading || !mHasMore) {
            return;
        }

        mIsLoading = true;

        if (mLoadMoreUIHandler != null && (mPage != 0 || mShowLoadingForFirstPage)) {
            mLoadMoreUIHandler.onLoading(this);
        }
        if (null != mLoadMoreHandler) {
            mLoadMoreHandler.onLoadMore(this);
        }
    }

    private void onReachBottom() {
        if (mAutoLoadMore) {
            performLoadMore();
        } else {
            mLoadMoreUIHandler.onWaitToLoadMore(this);
        }
    }

    @Override
    public void setShowLoadingForFirstPage(boolean showLoading) {
        mShowLoadingForFirstPage = showLoading;
    }

    @Override
    public void setAutoLoadMore(boolean autoLoadMore) {
        mAutoLoadMore = autoLoadMore;
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    public void setLoadMoreView(View view) {
        // has not been initialized
        if (mListView == null) {
            mFooterView = view;
            return;
        }
        // remove previous
        if (mFooterView != null && mFooterView != view) {
            mListView.removeFooterView(mFooterView);
        }

        // add current
        mFooterView = view;
        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                performLoadMore();
            }
        });
        mListView.addFooterView(view);
    }

    @Override
    public void setLoadMoreUIHandler(LoadMoreUIHandler handler) {
        mLoadMoreUIHandler = handler;
    }

    @Override
    public void setLoadMoreHandler(LoadMoreHandler handler) {
        mLoadMoreHandler = handler;
    }

    /**
     * page has loaded
     *
     * @param page    start from 0
     * @param hasMore
     */
    @Override
    public void loadMoreFinish(int page, boolean hasMore) {
        mPage = page;
        mIsLoading = false;
        mHasMore = hasMore;

        if (mLoadMoreUIHandler != null) {
            mLoadMoreUIHandler.onLoadFinish(this, page, hasMore);
        }
    }
}