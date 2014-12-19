package com.etao.mobile.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.etao.mobile.search.will.views.ListFooterView;
import com.taobao.etao.R;
import in.srain.cube.views.GridViewWithHeaderAndFooter;
import in.srain.cube.views.list.ListPageInfo;

/**
 * @author huqiu.lhq
 */
public class LoadMoreGridViewContainer extends FrameLayout implements ILoadMore {

    private AbsListView.OnScrollListener mOnScrollListener;
    private LoadMoreHandler mLoadMoreHandler;

    private ListFooterView mFooterView;

    private boolean mIsLoading;
    private boolean mHasMore = true;

    private int mStatusStringFirstLoading = R.string.base_loadmore_loading;
    private int mStatusStringLoadingMore = R.string.base_loadmore_loading;
    private int mStatusStringNoMore = R.string.base_loadmore_no_more;
    private int mStatusStringEmpty = R.string.base_loadmore_empty;
    private GridViewWithHeaderAndFooter mGridView;
    private int mGridViewId = 0;

    public LoadMoreGridViewContainer(Context context) {
        this(context, null);
    }

    public LoadMoreGridViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.LoadMoreGridViewContainer, 0, 0);
        if (arr != null) {
            mGridViewId = arr.getResourceId(R.styleable.LoadMoreGridViewContainer_load_more_grid_view, 0);
            if (mGridViewId == 0) {
                throw new RuntimeException("can not find load_more_grid_view has not been set");
            }
            arr.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = findViewById(mGridViewId);
        if (view == null || !(view instanceof GridViewWithHeaderAndFooter)) {
            throw new RuntimeException("GridView in you layout is null or not instance of GridViewWithHeaderAndFooter");
        }
        mGridView = (GridViewWithHeaderAndFooter) findViewById(mGridViewId);
        init();
    }

    private void init() {

        mFooterView = new ListFooterView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        mGridView.addFooterView(mFooterView);

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

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

        mFooterView.setVisibility(VISIBLE);
        mFooterView.setText(mStatusStringFirstLoading);
    }

    private void onReachBottom() {
        if (null != mLoadMoreHandler && !mIsLoading && mHasMore) {
            mIsLoading = true;
            if (mStatusStringLoadingMore < 0) {
                mFooterView.setVisibility(GONE);
            } else {
                mFooterView.setVisibility(VISIBLE);
                mFooterView.setText(mStatusStringLoadingMore);
                mGridView.post(new Runnable() {
                    @Override
                    public void run() {
                        // mGridView.tryToScrollToBottomSmoothly(100);
                        mGridView.setSelection(mGridView.getAdapter().getCount());
                    }
                });
                mGridView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLoadMoreHandler.onLoadMore();
                    }
                }, 110);
            }
        }
    }

    @Override
    public void loadFinish(ListPageInfo<?> page) {
        mHasMore = page.hasMore();
        mIsLoading = false;
        if (mHasMore) {
            mFooterView.setVisibility(GONE);
        } else {
            if (page.isEmpty()) {
                if (mStatusStringEmpty < 0) {
                    mFooterView.setVisibility(GONE);
                } else {
                    mFooterView.setVisibility(VISIBLE);
                    mFooterView.setText(mStatusStringEmpty);
                }
            } else {
                if (mStatusStringNoMore < 0) {
                    mFooterView.setVisibility(GONE);
                } else {
                    mFooterView.setVisibility(VISIBLE);
                    mFooterView.setText(mStatusStringNoMore);
                }
            }
        }
    }

    @Override
    public void setStatusString(int firstLoading, int loadingMore, int empty, int noMore) {
        if (firstLoading > 0) {
            mStatusStringFirstLoading = firstLoading;
        }
        if (loadingMore != 0) {
            mStatusStringLoadingMore = loadingMore;
        }
        if (empty != 0) {
            mStatusStringEmpty = empty;
        }
        if (noMore != 0) {
            mStatusStringNoMore = noMore;
        }
    }

    @Override
    public void setLoadMoreHandler(LoadMoreHandler loadMoreHandler) {
        mLoadMoreHandler = loadMoreHandler;
    }

    @Override
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    public ListFooterView getFooterStatusView() {
        return mFooterView;
    }

    public GridViewWithHeaderAndFooter getGridView() {
        return mGridView;
    }
}