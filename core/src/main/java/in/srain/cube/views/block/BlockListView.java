package in.srain.cube.views.block;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class BlockListView extends RelativeLayout {

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    private static final int INDEX_TAG = 0x04 << 24;

    private BlockListAdapter<?> mBlockListAdapter;

    private LayoutInflater mLayoutInflater;

    private OnItemClickListener mOnItemClickListener;

    public BlockListView(Context context) {
        this(context, null, 0);
    }

    public BlockListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BlockListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setAdapter(BlockListAdapter<?> adapter) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter should not be null");
        }
        mBlockListAdapter = adapter;
        adapter.registerView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (null != mBlockListAdapter) {
            mBlockListAdapter.registerView(null);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (null != mBlockListAdapter) {
            mBlockListAdapter.registerView(this);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int index = (Integer) v.getTag(INDEX_TAG);
            if (null != mOnItemClickListener) {
                mOnItemClickListener.onItemClick(v, index);
            }
        }
    };

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onDataListChange() {

        removeAllViews();

        int len = mBlockListAdapter.getCount();
        int w = mBlockListAdapter.getBlockWidth();
        int h = mBlockListAdapter.getBlockHeight();
        int columnNum = mBlockListAdapter.getCloumnNum();

        int horizontalSpacing = mBlockListAdapter.getHorizontalSpacing();
        int verticalSpacing = mBlockListAdapter.getVerticalSpacing();

        boolean blockDescendant = getDescendantFocusability() == ViewGroup.FOCUS_BLOCK_DESCENDANTS;

        for (int i = 0; i < len; i++) {

            RelativeLayout.LayoutParams lyp = new RelativeLayout.LayoutParams(w, h);
            int row = i / columnNum;
            int clo = i % columnNum;
            int left = 0;
            int top = 0;

            if (clo > 0) {
                left = (horizontalSpacing + w) * clo;
            }
            if (row > 0) {
                top = (verticalSpacing + h) * row;
            }
            lyp.setMargins(left, top, 0, 0);
            View view = mBlockListAdapter.getView(mLayoutInflater, i);
            if (!blockDescendant) {
                view.setOnClickListener(mOnClickListener);
            }
            view.setTag(INDEX_TAG, i);
            addView(view, lyp);
        }
        requestLayout();
    }
}
