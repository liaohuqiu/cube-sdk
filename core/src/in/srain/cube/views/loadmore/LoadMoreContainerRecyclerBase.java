package in.srain.cube.views.loadmore;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * @author huqiu.lhq
 */
public abstract class LoadMoreContainerRecyclerBase extends LinearLayout implements LoadMoreContainer<RecyclerView.OnScrollListener> {

    public enum LAYOUT_MANAGER_TYPE {
        LINEAR,
        GRID,
        STAGGERED_GRID
    }

    private RecyclerView.OnScrollListener mOnScrollListener;

    private LoadMoreUIHandler mLoadMoreUIHandler;
    private LoadMoreHandler mLoadMoreHandler;

    private boolean mIsLoading;
    private boolean mHasMore = false;
    private boolean mAutoLoadMore = true;
    private boolean mLoadError = false;

    private boolean mListEmpty = true;
    private boolean mShowLoadingForFirstPage = false;
    private View mFooterView;

    private RecyclerView mRecyclerView;

    public LoadMoreContainerRecyclerBase(Context context) {
        super(context);
    }

    public LoadMoreContainerRecyclerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRecyclerView = retrieveRecyclerView();
        init();
    }

    /**
     * @deprecated It's totally wrong. Use {@link #useDefaultFooter} instead.
     */
    @Deprecated
    public void useDefaultHeader() {
        useDefaultFooter();
    }

    public void useDefaultFooter() {
        LoadMoreDefaultFooterView footerView = new LoadMoreDefaultFooterView(getContext());
        footerView.setVisibility(GONE);
        setLoadMoreView(footerView);
        setLoadMoreUIHandler(footerView);
    }

    private void init() {

        if (mFooterView != null) {
            addFooterView(mFooterView);
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            private int lastVisibleItemPosition;
            private int[] lastPositions;
            private LAYOUT_MANAGER_TYPE layoutManagerType;

            @Override
            public void onScrollStateChanged(RecyclerView view, int newState) {
                if (null != mOnScrollListener) {
                    mOnScrollListener.onScrollStateChanged(view, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView view, int dx, int dy) {
                if (null != mOnScrollListener) {
                    mOnScrollListener.onScrolled(view, dx, dy);
                }
                RecyclerView.LayoutManager layoutManager = view.getLayoutManager();
                if (null != layoutManager) {
                    if (layoutManager instanceof LinearLayoutManager) {
                        layoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR;
                    } else if (layoutManager instanceof GridLayoutManager) {
                        layoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        layoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID;
                    } else {
                        throw new ClassCastException(
                                "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
                    }

                    switch (layoutManagerType) {
                        case LINEAR:
                            lastVisibleItemPosition = ((LinearLayoutManager) layoutManager)
                                    .findLastVisibleItemPosition();
                            break;
                        case GRID:
                            lastVisibleItemPosition = ((GridLayoutManager) layoutManager)
                                    .findLastVisibleItemPosition();
                            break;
                        case STAGGERED_GRID:
                            StaggeredGridLayoutManager staggeredGridLayoutManager
                                    = (StaggeredGridLayoutManager) layoutManager;
                            lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                            staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                            lastVisibleItemPosition = findMax(lastPositions);
                            break;
                    }

                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    if ((visibleItemCount > 0 && (lastVisibleItemPosition) >= totalItemCount - 1)) {
                        onReachBottom();
                    }
                }
            }

        });
    }

    private void tryToPerformLoadMore() {
        if (mIsLoading) {
            return;
        }

        // no more content and also not load for first page
        if (!mHasMore && !(mListEmpty && mShowLoadingForFirstPage)) {
            return;
        }

        mIsLoading = true;

        if (mLoadMoreUIHandler != null) {
            mLoadMoreUIHandler.onLoading(this);
        }
        if (null != mLoadMoreHandler) {
            mLoadMoreHandler.onLoadMore(this);
        }
    }

    private void onReachBottom() {
        // if has error, just leave what it should be
        if (mLoadError) {
            return;
        }
        if (mAutoLoadMore) {
            tryToPerformLoadMore();
        } else {
            if (mHasMore) {
                mLoadMoreUIHandler.onWaitToLoadMore(this);
            }
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
    public void setOnScrollListener(RecyclerView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    public void setLoadMoreView(View view) {
        // has not been initialized
        if (mRecyclerView == null) {
            mFooterView = view;
            return;
        }
        // remove previous
        if (mFooterView != null && mFooterView != view) {
            removeFooterView(view);
        }

        // add current
        mFooterView = view;
        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToPerformLoadMore();
            }
        });

        addFooterView(view);
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
     * @param emptyResult
     * @param hasMore
     */
    @Override
    public void loadMoreFinish(boolean emptyResult, boolean hasMore) {
        mLoadError = false;
        mListEmpty = emptyResult;
        mIsLoading = false;
        mHasMore = hasMore;

        if (mLoadMoreUIHandler != null) {
            mLoadMoreUIHandler.onLoadFinish(this, emptyResult, hasMore);
        }
    }

    @Override
    public void loadMoreError(int errorCode, String errorMessage) {
        mIsLoading = false;
        mLoadError = true;
        if (mLoadMoreUIHandler != null) {
            mLoadMoreUIHandler.onLoadError(this, errorCode, errorMessage);
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }


    protected abstract void addFooterView(View view);

    protected abstract void removeFooterView(View view);

    protected abstract RecyclerView retrieveRecyclerView();

}
