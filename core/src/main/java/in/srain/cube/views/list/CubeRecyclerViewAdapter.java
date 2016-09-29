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
public abstract class CubeRecyclerViewAdapter<ItemDataType> extends RecyclerView.Adapter<CubeRecyclerViewAdapter.InnerViewHolder> {

    private final int TYPE_OFFSET_HEADER = 10000;
    private final int TYPE_OFFSET_FOOTER = 20000;

    // 2015-05-21
    private final int TAG_KEY_FOR_INDEX = 521 << 24;

    protected SparseArray<ViewHolderCreator<ItemDataType>> mLazyCreators = new SparseArray<ViewHolderCreator<ItemDataType>>();

    private List<ItemDataType> mList;

    private List<StaticViewHolder> mHeaderViews = new ArrayList<StaticViewHolder>();

    private List<StaticViewHolder> mFooterViews = new ArrayList<StaticViewHolder>();

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
            return mHeaderViews.get(viewType - TYPE_OFFSET_HEADER);
        }
        ViewHolderCreator<ItemDataType> creator = mLazyCreators.get(viewType);
        ViewHolderBase<ItemDataType> cubeViewHolder = creator.createViewHolder(-1);

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = cubeViewHolder.createView(inflater, parent);
        if (v != null && mOnItemClickListener != null) {
            v.setOnClickListener(mOnClickListener);
        }
        InnerViewHolder viewHolder = new InnerViewHolder<ItemDataType>(v);
        viewHolder.mCubeViewHolder = cubeViewHolder;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(InnerViewHolder holder, int position) {
        if (holder != null && holder.mCubeViewHolder != null) {
            position = positionForDataItem(position);
            if (holder.itemView != null) {
                holder.itemView.setTag(TAG_KEY_FOR_INDEX, position);
            }
            holder.mCubeViewHolder.showData(position, getDataItem(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        int type = doGetItemViewType(position);
        return type;
    }

    @Override
    public int getItemCount() {
        return getDataItemCount() + getHeaderViewCount() + getFooterViewCount();
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

    protected abstract int getDataItemViewType(int position);

    public int getDataItemCount() {
        if (mList == null) {
            return 0;
        }
        return mList.size();
    }

    public void addHeaderView(View view) {
        for (int i = 0; i < mHeaderViews.size(); i++) {
            StaticViewHolder holder = mHeaderViews.get(i);
            if (holder.itemView == view) {
                return;
            }
        }
        mHeaderViews.add(new StaticViewHolder(view));
    }

    public void addFooterView(View view) {
        for (int i = 0; i < mFooterViews.size(); i++) {
            StaticViewHolder holder = mFooterViews.get(i);
            if (holder.itemView == view) {
                return;
            }
        }
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

    public ItemDataType getDataItem(int position) {
        if (mList == null || position < 0 || position >= mList.size()) {
            return null;
        }
        return mList.get(position);
    }

    public interface OnItemClickListener {
        public void onClick(View view, int position);
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
