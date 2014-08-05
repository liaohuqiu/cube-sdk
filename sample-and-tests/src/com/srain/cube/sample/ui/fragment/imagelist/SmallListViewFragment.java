package net.liaohuqiu.cube.sample.ui.fragment.imagelist;

import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import net.liaohuqiu.cube.image.CubeImageView;
import net.liaohuqiu.cube.image.ImageLoader;
import net.liaohuqiu.cube.image.ImageLoaderFactory;
import net.liaohuqiu.cube.image.ImageReuseInfo;
import net.liaohuqiu.cube.image.imple.DefaultImageLoadHandler;
import net.liaohuqiu.cube.sample.R;
import net.liaohuqiu.cube.sample.activity.TitleBaseFragment;
import net.liaohuqiu.cube.sample.data.Images;
import net.liaohuqiu.cube.util.LocalDisplay;
import net.liaohuqiu.cube.views.IScrollHeaderFrameHandler;
import net.liaohuqiu.cube.views.ScrollHeaderFrame;
import net.liaohuqiu.cube.views.list.ListViewDataAdapter;
import net.liaohuqiu.cube.views.list.ViewHolderBase;
import net.liaohuqiu.cube.views.list.ViewHolderCreator;

public class SmallListViewFragment extends TitleBaseFragment {

    private ImageLoader mImageLoader;
    public static final int sSmallImageSize = LocalDisplay.dp2px(100);
    private ListView mListView;

    private static final ImageReuseInfo sSmallImageReuseInfo = Images.sImageReuseInfoManger.create("small_180");

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mImageLoader = ImageLoaderFactory.create(getActivity());
        ((DefaultImageLoadHandler) mImageLoader.getImageLoadHandler()).setImageRounded(true, 25);

        final View v = inflater.inflate(R.layout.fragment_image_list_small, container, false);

        mListView = (ListView) v.findViewById(R.id.ly_image_list_small);

        ListViewDataAdapter<String> adapter = new ListViewDataAdapter<String>(new ViewHolderCreator<String>() {
            @Override
            public ViewHolderBase<String> createViewHolder() {
                return new ViewHolder();
            }
        });
        mListView.setAdapter(adapter);
        adapter.getDataList().addAll(Arrays.asList(Images.imageUrls));
        adapter.notifyDataSetChanged();

        setHeaderTitle("Small List");
        ((ScrollHeaderFrame) mContentContainer.getParent()).setHandler(mScrollHeaderFrameHandler);
        return v;
    }

    private class ViewHolder extends ViewHolderBase<String> {

        private CubeImageView mImageView;

        @Override
        public View createView(LayoutInflater inflater) {
            View v = inflater.inflate(R.layout.item_image_list_small, null);
            mImageView = (CubeImageView) v.findViewById(R.id.tv_item_image_list_small);
            mImageView.setScaleType(ScaleType.CENTER_CROP);
            return v;
        }

        @Override
        public void showData(int position, String itemData) {
            mImageView.loadImage(mImageLoader, itemData, sSmallImageReuseInfo);
        }
    }

    private IScrollHeaderFrameHandler mScrollHeaderFrameHandler = new IScrollHeaderFrameHandler() {
        @Override
        public boolean hasReachTop() {
            return mListView.getFirstVisiblePosition() == 0;
        }
    };
}
