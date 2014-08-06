package in.srain.cube.sample.ui.fragment.imagelist;

import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.ImageReuseInfo;
import in.srain.cube.image.imple.DefaultImageLoadHandler;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.TitleBaseFragment;
import in.srain.cube.sample.data.Images;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.IScrollHeaderFrameHandler;
import in.srain.cube.views.ScrollHeaderFrame;
import in.srain.cube.views.list.ListViewDataAdapter;
import in.srain.cube.views.list.ViewHolderBase;
import in.srain.cube.views.list.ViewHolderCreator;

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
