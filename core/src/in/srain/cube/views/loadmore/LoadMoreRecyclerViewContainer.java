package in.srain.cube.views.loadmore;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import in.srain.cube.views.list.RecyclerViewAdapterBase;

/**
 * @author audiebant
 */
public class LoadMoreRecyclerViewContainer extends LoadMoreContainerRecyclerBase {

    private RecyclerView mRecyclerView;

    public LoadMoreRecyclerViewContainer(Context context) {
        super(context);
    }

    public LoadMoreRecyclerViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void addFooterView(View view) {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        ((RecyclerViewAdapterBase) adapter).addFooterView(view);
    }

    @Override
    protected void removeFooterView(View view) {
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        ((RecyclerViewAdapterBase) adapter).removeFooterView(view);
    }


    @Override
    protected RecyclerView retrieveRecyclerView() {
        mRecyclerView = (RecyclerView) getChildAt(0);
        return mRecyclerView;
    }

}
