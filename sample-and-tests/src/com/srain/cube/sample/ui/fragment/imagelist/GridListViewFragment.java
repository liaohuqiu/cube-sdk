package net.liaohuqiu.cube.sample.ui.fragment.imagelist;

import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import net.liaohuqiu.cube.image.CubeImageView;
import net.liaohuqiu.cube.image.ImageLoader;
import net.liaohuqiu.cube.image.ImageLoaderFactory;
import net.liaohuqiu.cube.image.ImageReuseInfo;
import net.liaohuqiu.cube.sample.R;
import net.liaohuqiu.cube.sample.activity.TitleBaseFragment;
import net.liaohuqiu.cube.sample.data.Images;
import net.liaohuqiu.cube.util.LocalDisplay;
import net.liaohuqiu.cube.views.list.ListViewDataAdapter;
import net.liaohuqiu.cube.views.list.ViewHolderBase;
import net.liaohuqiu.cube.views.list.ViewHolderCreator;

public class GridListViewFragment extends TitleBaseFragment {

    private static final int sGirdImageSize = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(10 + 10 + 10)) / 2;
    private ImageLoader mImageLoader;

    private static final ImageReuseInfo sGridImageReuseInfo = Images.sImageReuseInfoManger.create("big_360");

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mImageLoader = ImageLoaderFactory.create(getActivity());

        final View v = inflater.inflate(R.layout.fragment_image_gird, container, false);
        GridView gridListView = (GridView) v.findViewById(R.id.ly_image_list_grid);

        ListViewDataAdapter<String> adapter = new ListViewDataAdapter<String>(new ViewHolderCreator<String>() {
            @Override
            public ViewHolderBase<String> createViewHolder() {
                return new ViewHolder();
            }
        });
        gridListView.setAdapter(adapter);
        adapter.getDataList().addAll(Arrays.asList(Images.imageUrls));
        adapter.notifyDataSetChanged();
        setHeaderTitle("Grid");
        return v;
    }

    private class ViewHolder extends ViewHolderBase<String> {

        private CubeImageView mImageView;

        @Override
        public View createView(LayoutInflater inflater) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.item_image_list_grid, null);
            mImageView = (CubeImageView) view.findViewById(R.id.iv_item_iamge_list_grid);
            mImageView.setScaleType(ScaleType.CENTER_CROP);

            LinearLayout.LayoutParams lyp = new LinearLayout.LayoutParams(sGirdImageSize, sGirdImageSize);
            mImageView.setLayoutParams(lyp);
            return view;
        }

        @Override
        public void showData(int position, String itemData) {
            if (position == 0) {
                int a = 0;
                if (a == 0) {

                }
            }
            mImageView.loadImage(mImageLoader, itemData, sGridImageReuseInfo);
        }
    }
}
