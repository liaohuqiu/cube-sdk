package in.srain.cube.views.loadmore;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;

/**
 * @author huqiu.lhq
 *
 * @modify qsl
 * @phone 15618965173
 */
public class LoadMoreScrollViewContainer extends LinearLayout implements LoadMoreContainer {

    private AbsListView.OnScrollListener mOnScrollListener;
    private LoadMoreUIHandler mLoadMoreUIHandler;
    private LoadMoreHandler mLoadMoreHandler;

    private boolean mIsLoading;
    private boolean mHasMore = false;
    private boolean mAutoLoadMore = true;
    private boolean mLoadError = false;

    private boolean mListEmpty = true;
    private boolean mShowLoadingForFirstPage = false;
    private View mFooterView;

    private CubeScrollView cubeScrollView;

    public LoadMoreScrollViewContainer(Context context) {
        super(context);
    }

    public LoadMoreScrollViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        cubeScrollView = (CubeScrollView) retrieveAbsListView();
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
        cubeScrollView.setOnScrollChangedListener(new CubeScrollView.OnScrollChangedListener() {
            private boolean mIsEnd = false;

            @Override
            public void onScrollChange(int Y, int Yrange) {
                if (Y >= Yrange) {
                    mIsEnd = true;
                } else {
                    mIsEnd = false;
                }
            }

			@Override
			public void onActionUp() {
				// TODO Auto-generated method stub
				Log.e("errrrr", "upuppuup");
				 if (mIsEnd) {
						onReachBottom();
						cubeScrollView.fullScroll(View.FOCUS_DOWN);
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
    public void setOnScrollListener(AbsListView.OnScrollListener l) {
        mOnScrollListener = l;
    }

    @Override
    public void setLoadMoreView(View view) {
        // has not been initialized
        if (cubeScrollView == null) {
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

    protected void addFooterView(View view) {
        ViewGroup vg = (ViewGroup) ((ViewGroup) getChildAt(0)).getChildAt(0);
        vg.addView(view);
    }

    protected void removeFooterView(View view) {
        ViewGroup vg = (ViewGroup) ((ViewGroup) getChildAt(0)).getChildAt(0);
        vg.removeView(view);
    }

    protected View retrieveAbsListView() {
        return getChildAt(0);
    }
}