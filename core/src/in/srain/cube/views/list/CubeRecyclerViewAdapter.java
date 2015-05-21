package in.srain.cube.views.list;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Supports addHeaderView / addFooterView like {@link ListView#addHeaderView}
 *
 * @param <ItemDataType>
 */
public class CubeRecyclerViewAdapter<ItemDataType> extends RecyclerView.Adapter<CubeRecyclerViewAdapter.InnerViewHolder> {

    private final int TYPE_OFFSET_HEADER = 10000;
    private final int TYPE_OFFSET_FOOTER = 20000;
    protected SparseArray<ViewHolderCreator<ItemDataType>> mLazyCreators = new SparseArray<ViewHolderCreator<ItemDataType>>();
    private List<ItemDataType> mList;
    private List<StaticViewHolder> mHeaderViews = new ArrayList<StaticViewHolder>();
    private List<StaticViewHolder> mFooterViews = new ArrayList<StaticViewHolder>();

    public void setViewHolderClass(int viewType, final Object enclosingInstance, final Class<?> cls, final Object... args) {
        ViewHolderCreator<ItemDataType> lazyCreator = LazyViewHolderCreator.create(enclosingInstance, cls, args);
        mLazyCreators.put(viewType, lazyCreator);
    }

    public void setList(List<ItemDataType> list) {
        mList = list;
    }

    @Override
    public InnerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType >= TYPE_OFFSET_FOOTER) {
            return mFooterViews.get(viewType - TYPE_OFFSET_FOOTER);
        }
        if (viewType >= TYPE_OFFSET_HEADER) {
            return mFooterViews.get(viewType - TYPE_OFFSET_HEADER);
        }
        ViewHolderCreator<ItemDataType> creator = mLazyCreators.get(viewType);
        ViewHolderBase<ItemDataType> cubeViewHolder = creator.createViewHolder(-1);

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = cubeViewHolder.createView(inflater);
        InnerViewHolder viewHolder = new InnerViewHolder<ItemDataType>(v);
        viewHolder.mCubeViewHolder = cubeViewHolder;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InnerViewHolder holder, int position) {
        if (holder != null && holder.mCubeViewHolder != null) {
            holder.mCubeViewHolder.showData(position, getDataItem(position));
        }
    }

    @Override
    public int getItemViewType(int position) {

        final int headerViewCount = getHeaderViewCount();
        final int footerViewCount = getFooterViewCount();
        final int dataTotal = getDataItemCount();

        if (headerViewCount > 0) {
            if (position < headerViewCount) {
                return TYPE_OFFSET_HEADER + position;
            }
        }

        position = position - headerViewCount;
        final int dataPosition = position;
        if (position < dataTotal) {
            return super.getItemViewType(position);
        } else {
            position = position - dataTotal;
            if (position < footerViewCount) {
                return TYPE_OFFSET_FOOTER + position;
            }

            if (position == 0) {
                return 0;
            }
            throw new IndexOutOfBoundsException("Invalid index " + dataPosition + ", size is " + getDataItemCount());
        }
    }

    @Override
    public int getItemCount() {
        return getDataItemCount() + getHeaderViewCount() + getFooterViewCount();
    }

    public int getDataItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void addHeaderView(View view) {
        mHeaderViews.add(new StaticViewHolder(view));
    }

    public void addFooterView(View view) {
        mFooterViews.add(new StaticViewHolder(view));
    }

    public void removeFooterView(View view) {

    }

    public int getHeaderViewCount() {
        return mHeaderViews.size();
    }

    public int getFooterViewCount() {
        return mFooterViews.size();
    }

    private ItemDataType getDataItem(int position) {
        if (mList == null || position < 0 || position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }

    static class InnerViewHolder<TT> extends RecyclerView.ViewHolder {

        ViewHolderBase<TT> mCubeViewHolder;

        public InnerViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class StaticViewHolder extends InnerViewHolder<Object> {

        public StaticViewHolder(View itemView) {
            super(itemView);
        }
    }
}
