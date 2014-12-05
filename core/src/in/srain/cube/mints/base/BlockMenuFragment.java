package in.srain.cube.mints.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import in.srain.cube.R;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.block.BlockListAdapter;
import in.srain.cube.views.block.BlockListView;
import in.srain.cube.views.block.BlockListView.OnItemClickListener;

import java.util.ArrayList;

public abstract class BlockMenuFragment extends TitleBaseFragment {

    private BlockListView mBlockListView;
    private ArrayList<ItemInfo> mItemInfos = new ArrayList<BlockMenuFragment.ItemInfo>();
    private int mSize = 0;
    private BlockListAdapter<ItemInfo> mBlockListAdapter = new BlockListAdapter<BlockMenuFragment.ItemInfo>() {

        @Override
        public View getView(LayoutInflater layoutInflater, int position) {
            return getViewForBlock(layoutInflater, position);
        }
    };

    protected int getLayoutId() {
        return R.layout.cube_mints_base_fragment_block_menu;
    }

    protected View getViewForBlock(LayoutInflater layoutInflater, int position) {
        ItemInfo itemInfo = mBlockListAdapter.getItem(position);

        ViewGroup view = (ViewGroup) layoutInflater.inflate(R.layout.cube_mints_base_block_menu_item, null);

        if (itemInfo != null) {
            TextView textView = ((TextView) view.findViewById(R.id.cube_mints_base_block_menu_item_title));
            textView.setText(itemInfo.mTitle);
            view.setBackgroundColor(itemInfo.getColor());
        }
        return view;
    }

    protected abstract void addItemInfo(ArrayList<ItemInfo> itemInfos);

    protected abstract void setupViews();

    @Override
    protected View createView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), null);
        mBlockListView = (BlockListView) view.findViewById(R.id.fragment_block_menu_block_list);
        addItemInfo(mItemInfos);
        setupList();
        setupViews();
        return view;
    }

    protected void setupList() {

        mSize = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(25 + 5 + 5)) / 3;

        int horizontalSpacing = LocalDisplay.dp2px(5);
        int verticalSpacing = LocalDisplay.dp2px(10.5f);

        mBlockListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                ItemInfo itemInfo = mBlockListAdapter.getItem(position);
                if (itemInfo != null) {
                    itemInfo.mOnClickListener.onClick(v);
                }
            }
        });

        mBlockListAdapter.setSpace(horizontalSpacing, verticalSpacing);
        mBlockListAdapter.setBlockSize(mSize, mSize);
        mBlockListAdapter.setColumnNum(3);
        mBlockListView.setAdapter(mBlockListAdapter);
        mBlockListAdapter.displayBlocks(mItemInfos);
    }

    protected ItemInfo newItemInfo(String title, int color, OnClickListener onClickListener) {
        return new ItemInfo(title, getResources().getColor(color), onClickListener);
    }

    protected ItemInfo newItemInfo(String title, String color, OnClickListener onClickListener) {
        return new ItemInfo(title, Color.parseColor(color), onClickListener);
    }

    protected ItemInfo newItemInfo(int title, String color, OnClickListener onClickListener) {
        return new ItemInfo(getString(title), Color.parseColor(color), onClickListener);
    }

    protected ItemInfo newItemInfo(int title, int color, OnClickListener onClickListener) {
        return new ItemInfo(getString(title), getResources().getColor(color), onClickListener);
    }

    @Override
    protected boolean enableDefaultBack() {
        return false;
    }

    protected static class ItemInfo {
        private int mColor;
        private String mTitle;
        private OnClickListener mOnClickListener;

        public ItemInfo(String title, int color, OnClickListener onClickListener) {
            mTitle = title;
            mColor = color;
            mOnClickListener = onClickListener;
        }

        private int getColor() {
            return mColor;
        }
    }
}
