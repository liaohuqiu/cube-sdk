package in.srain.cube.views.list;

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author audiebant
 */
public abstract class RecyclerViewAdapterBase <V extends ViewHolder> extends RecyclerView.Adapter<ViewHolder> {

    private View mFooterView;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM)
              bindView((V)holder, position);
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

    protected abstract V getView(LayoutInflater inflater, int viewType);

    protected abstract void bindView(V holder, int position);

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View v) {
            super(v);
        }
    }

}
