package in.srain.cube.views.list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author audiebant
 */
public abstract class RecyclerViewAdapterBase extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private View mFooterView;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return getView(inflater, viewType);
        } else if (viewType == TYPE_FOOTER) {
            View v = mFooterView;
            return new FooterViewHolder(v);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM)
            bindView(holder, position);
    }


    @Override
    public int getItemViewType(int position) {
        if (position + getFooterViewSize() == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return getDataItemSize();
    }

    private int getFooterViewSize() {
        return mFooterView == null ? 0 : 1;
    }

    public void addFooterView(View footer) {
        mFooterView = footer;
        notifyItemInserted(getItemCount());
    }

    public void removeFooterView(View footer) {
        notifyItemRemoved(getItemCount());
        mFooterView = null;
    }

    protected abstract int getDataItemSize();

    protected abstract RecyclerView.ViewHolder getView(LayoutInflater inflater, int viewType);

    protected abstract void bindView(RecyclerView.ViewHolder holder, int position);

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View v) {
            super(v);
        }
    }

}
