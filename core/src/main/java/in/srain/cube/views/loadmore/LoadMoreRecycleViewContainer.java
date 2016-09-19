package in.srain.cube.views.loadmore;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import in.srain.cube.views.list.CubeRecyclerViewAdapter;

/**
 * RecyclerView also wanna endless scroll to load more data.
 */
public class LoadMoreRecycleViewContainer extends LoadMoreContainerBase {

    private RecyclerView mRecyclerView;
    private CubeRecyclerViewAdapter<?> mAdapter;
    private RecyclerView.OnScrollListener mOnScrollListener;

    // for memory profile
    private LinearLayoutManager mLinearLayoutManager;

    public LoadMoreRecycleViewContainer(Context context) {
        super(context);
    }

    public LoadMoreRecycleViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View view = getChildAt(0);
        mRecyclerView = (RecyclerView) view;
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            // for memory profile
            int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrollStateChanged(recyclerView, newState);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mOnScrollListener != null) {
                    mOnScrollListener.onScrolled(recyclerView, dx, dy);
                }
                getLinearLayoutManager();
                if (mLinearLayoutManager == null) {
                    return;
                }

                mVisibleItemCount = recyclerView.getChildCount();
                mTotalItemCount = mLinearLayoutManager.getItemCount();
                mFirstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

                if ((mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem + mVisibleThreshold)) {
                    onReachBottom();
                }
            }
        });
    }

    @Override
    protected void addFooterView(View view) {
        mAdapter.addFooterView(view);
    }

    @Override
    protected void removeFooterView(View view) {
        mAdapter.removeFooterView(view);
    }

    @Override
    protected Object retrieveListView() {
        return getRecyclerView();
    }

    protected LinearLayoutManager getLinearLayoutManager() {
        if (mLinearLayoutManager != null) {
            return mLinearLayoutManager;
        }
        if (mRecyclerView == null) {
            return null;
        }
        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            mLinearLayoutManager = (LinearLayoutManager) layoutManager;
            return mLinearLayoutManager;
        }
        return null;
    }

    public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    public void setCubeRecyclerViewAdapter(CubeRecyclerViewAdapter<?> adapter) {
        mAdapter = adapter;
    }

    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }
}
