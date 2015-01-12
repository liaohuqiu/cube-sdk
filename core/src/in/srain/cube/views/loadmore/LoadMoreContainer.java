package in.srain.cube.views.loadmore;

import android.view.View;
import android.widget.AbsListView;

public interface LoadMoreContainer {

    public void setShowLoadingForFirstPage(boolean showLoading);

    public void setAutoLoadMore(boolean autoLoadMore);

    public void setOnScrollListener(AbsListView.OnScrollListener l);

    public void setLoadMoreView(View view);

    public void setLoadMoreUIHandler(LoadMoreUIHandler handler);

    public void setLoadMoreHandler(LoadMoreHandler handler);

    /**
     * page has loaded
     *
     * @param emptyResult
     * @param hasMore
     */
    public void loadMoreFinish(boolean emptyResult, boolean hasMore);
}
