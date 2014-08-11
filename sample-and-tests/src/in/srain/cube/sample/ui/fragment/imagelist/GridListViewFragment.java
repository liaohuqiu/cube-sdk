package in.srain.cube.sample.ui.fragment.imagelist;

import java.util.Arrays;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.ImageReuseInfo;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.TitleBaseFragment;
import in.srain.cube.sample.data.Images;
import in.srain.cube.sample.ui.views.header.ptr.PtrFrameDemo;
import in.srain.cube.util.CLog;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.list.ListViewDataAdapter;
import in.srain.cube.views.list.ViewHolderBase;
import in.srain.cube.views.list.ViewHolderCreator;
import in.srain.cube.views.ptr.PtrFrame;

public class GridListViewFragment extends TitleBaseFragment {

    private static final int sGirdImageSize = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(12 + 12 + 10)) / 2;
    private ImageLoader mImageLoader;

    private static final ImageReuseInfo sGridImageReuseInfo = Images.sImageReuseInfoManger.create("big_360");

    @Override
    public View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mImageLoader = ImageLoaderFactory.create(getActivity());

        final View v = inflater.inflate(R.layout.fragment_image_gird, container, false);
        final GridView gridListView = (GridView) v.findViewById(R.id.ly_image_list_grid);

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

        final PtrFrameDemo ptrFrame = (PtrFrameDemo) v.findViewById(R.id.ly_ptr_frame);
        ptrFrame.setKeepHeaderWhenRefresh(true);
        ptrFrame.setHandler(new PtrFrameDemo.Handler() {
            @Override
            public void onRefresh() {
                CLog.d("test", "onRefresh");
                ptrFrame.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        ptrFrame.onRefreshComplete();
                    }
                }, 1000);
            }

            @Override
            public boolean canDoRefresh() {
                View view = gridListView.getChildAt(0);
                return view.getTop() == 0;
            }
        });
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
