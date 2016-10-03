package in.srain.cube.views.list;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;

import java.util.ArrayList;
import java.util.List;

/**
 * Supports addHeaderView / addFooterView like {@link ListView#addHeaderView}
 *
 * @param <ItemDataType>
 */
public class CubeRecyclerViewAdapter<ItemDataType> extends RecyclerView.Adapter<CubeRecyclerViewAdapter.ViewHolderProxy> {

    private final int TYPE_OFFSET_HEADER = 10000;
    private final int TYPE_OFFSET_FOOTER = 20000;

    // 2015-05-21
    private final int TAG_KEY_FOR_INDEX = 521 << 24;

    protected SparseArray<LazyViewHolderCreator<ItemDataType>> mLazyCreators = new SparseArray<LazyViewHolderCreator<ItemDataType>>();

    private List<ItemDataType> mList;

    private List<StaticViewHolderProxy> mHeaderViews = new ArrayList<StaticViewHolderProxy>();

    private List<StaticViewHolderProxy> mFooterViews = new ArrayList<StaticViewHolderProxy>();

    private OnItemClickListener mOnItemClickListener;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Object obj = v.getTag(TAG_KEY_FOR_INDEX);
            if (obj == null) {
                return;
            }
            int position = (Integer) obj;
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onClick(v, position);
            }
        }
    };

    public void setOnItemClickListener(OnItemClickListener handler) {
        mOnItemClickListener = handler;
    }

    public void setViewHolderClass(int viewType, final Object enclosingInstance, final Class<?> cls, int maxRecycledViews, final Object... args) {
        LazyViewHolderCreator<ItemDataType> lazyCreator = LazyViewHolderCreator.create(enclosingInstance, cls, maxRecycledViews, args);
        mLazyCreators.put(viewType, lazyCreator);
    }

    public void setList(List<ItemDataType> list) {
        mList = list;
    }

    public RecyclerView.RecycledViewPool createRecycledViewPool(ViewGroup parentView, RecyclerView.RecycledViewPool recycledViewPool) {
        if (recycledViewPool == null) {
            recycledViewPool = new RecyclerView.RecycledViewPool();
        }
        for (int i = 0, nsize = mLazyCreators.size(); i < nsize; i++) {
            final int viewType = mLazyCreators.keyAt(i);
            LazyViewHolderCreator<ItemDataType> lazyViewHolderCreator = mLazyCreators.valueAt(i);
            final int maxRecycledViews = lazyViewHolderCreator.getMaxRecycledViews();
            recycledViewPool.setMaxRecycledViews(viewType, maxRecycledViews);
            for (int j = 0; j < maxRecycledViews; j++) {
                RecyclerView.ViewHolder viewHolder = createViewHolder(parentView, viewType);
                recycledViewPool.putRecycledView(viewHolder);
            }
        }
        return recycledViewPool;
    }

    @Override
    public ViewHolderProxy onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType >= TYPE_OFFSET_FOOTER) {
            return mFooterViews.get(viewType - TYPE_OFFSET_FOOTER);
        }
        if (viewType >= TYPE_OFFSET_HEADER) {
            return mHeaderViews.get(viewType - TYPE_OFFSET_HEADER);
        }
        final ViewHolderCreator<ItemDataType> creator = mLazyCreators.get(viewType);
        final ViewHolderBase<ItemDataType> cubeViewHolder = creator.createViewHolder(-1);

        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = cubeViewHolder.createView(inflater, parent);

        if (CubeDebug.DEBUG_LIST) {
            CLog.d(ListViewDataAdapterBase.LOG_TAG, "CubeRecyclerViewAdapter::createView: %s %s", cubeViewHolder, viewType);
        }
        if (v != null && mOnItemClickListener != null) {
            v.setOnClickListener(mOnClickListener);
        }
        final ViewHolderProxy viewHolder = new ViewHolderProxy<ItemDataType>(v);
        viewHolder.mCubeViewHolder = cubeViewHolder;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolderProxy holder, int position) {
        if (holder != null && holder.mCubeViewHolder != null) {
            position = positionForDataItem(position);
            if (holder.itemView != null) {
                holder.itemView.setTag(TAG_KEY_FOR_INDEX, position);
            }
            holder.mCubeViewHolder.showData(position, getDataItem(position));
        }
    }

    /**
     * Please use getDataItemViewType to return the view type;
     *
     * @param position
     * @return
     */
    @Override
    final public int getItemViewType(int position) {
        int type = doGetItemViewType(position);
        return type;
    }

    @Override
    public int getItemCount() {
        return getDataItemCount() + getHeaderViewCount() + getFooterViewCount();
    }

    public int innerPositionForDataItem(int position) {
        return position + getHeaderViewCount();
    }

    private int positionForDataItem(int position) {
        final int headerViewCount = getHeaderViewCount();
        final int dataTotal = getDataItemCount();

        if (headerViewCount > 0) {
            if (position < headerViewCount) {
                return -1;
            }
        }

        position = position - headerViewCount;
        if (position < dataTotal) {
            return position;
        } else {
            return -1;
        }
    }

    private int doGetItemViewType(int position) {

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
            return getDataItemViewType(position);
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

    protected int getDataItemViewType(int position) {
        return 0;
    }

    public int getDataItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void addHeaderView(View view) {
        for (int i = 0; i < mHeaderViews.size(); i++) {
            StaticViewHolderProxy holder = mHeaderViews.get(i);
            if (holder.itemView == view) {
                return;
            }
        }
        mHeaderViews.add(new StaticViewHolderProxy(view));
    }

    public void addFooterView(View view) {
        for (int i = 0; i < mFooterViews.size(); i++) {
            StaticViewHolderProxy holder = mFooterViews.get(i);
            if (holder.itemView == view) {
                return;
            }
        }
        mFooterViews.add(new StaticViewHolderProxy(view));
    }

    public void removeFooterView(View view) {

    }

    public int getHeaderViewCount() {
        return mHeaderViews.size();
    }

    public int getFooterViewCount() {
        return mFooterViews.size();
    }

    public ItemDataType getDataItem(int position) {
        if (mList == null || position < 0 || position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }

    public interface OnItemClickListener {
        void onClick(View view, int position);
    }

    static class ViewHolderProxy<TT> extends RecyclerView.ViewHolder {

        ViewHolderBase<TT> mCubeViewHolder;

        private ViewHolderProxy(View itemView, ViewHolderBase<TT> viewHolder) {
            this(itemView);
            mCubeViewHolder = viewHolder;
        }

        public ViewHolderProxy(View itemView) {
            super(itemView);
        }
    }

    static class StaticViewHolderProxy extends ViewHolderProxy<Object> {

        public StaticViewHolderProxy(View itemView) {
            super(itemView);
        }
    }
}
