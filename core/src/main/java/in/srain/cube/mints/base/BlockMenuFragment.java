package in.srain.cube.mints.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import in.srain.cube.R;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.block.BlockListAdapter;
import in.srain.cube.views.block.BlockListView;
import in.srain.cube.views.block.BlockListView.OnItemClickListener;

public abstract class BlockMenuFragment extends MenuItemFragment {

    private BlockListView mBlockListView;
    private int mSize = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.cube_mints_base_fragment_block_menu;
    }

    @Override
    protected void setupViews(View view) {
        mBlockListView = (BlockListView) view.findViewById(R.id.fragment_block_menu_block_list);
        setupList();
    }

    private BlockListAdapter<MenuItemInfo> mBlockListAdapter = new BlockListAdapter<MenuItemInfo>() {

        @Override
        public View getView(LayoutInflater layoutInflater, int position) {
            return getViewForBlock(layoutInflater, position);
        }
    };

    protected View getViewForBlock(LayoutInflater layoutInflater, int position) {
        MenuItemInfo itemInfo = mBlockListAdapter.getItem(position);

        ViewGroup view = (ViewGroup) layoutInflater.inflate(R.layout.cube_mints_base_block_menu_item, null);

        if (itemInfo != null) {
            TextView textView = ((TextView) view.findViewById(R.id.cube_mints_base_block_menu_item_title));
            textView.setText(itemInfo.getTitle());
            view.setBackgroundColor(itemInfo.getColor());
        }
        return view;
    }

    protected void setupList() {

        mSize = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(25 + 5 + 5)) / 3;

        int horizontalSpacing = LocalDisplay.dp2px(5);
        int verticalSpacing = LocalDisplay.dp2px(10.5f);

        mBlockListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(View v, int position) {
                MenuItemInfo itemInfo = mBlockListAdapter.getItem(position);
                if (itemInfo != null) {
                    itemInfo.onClick(v);
                }
            }
        });

        mBlockListAdapter.setSpace(horizontalSpacing, verticalSpacing);
        mBlockListAdapter.setBlockSize(mSize, mSize);
        mBlockListAdapter.setColumnNum(3);
        mBlockListView.setAdapter(mBlockListAdapter);
        mBlockListAdapter.displayBlocks(mItemInfos);
    }

    @Override
    protected boolean enableDefaultBack() {
        return false;
    }
}
