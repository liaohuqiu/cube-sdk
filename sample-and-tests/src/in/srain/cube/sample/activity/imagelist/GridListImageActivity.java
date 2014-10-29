package in.srain.cube.sample.activity.imagelist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.ImageReuseInfo;
import in.srain.cube.request.CacheAbleRequest;
import in.srain.cube.request.JsonData;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.base.TitleBaseActivity;
import in.srain.cube.sample.data.DemoRequestData;
import in.srain.cube.sample.data.Images;
import in.srain.cube.sample.ui.views.header.ptr.PtrFrameDemo;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.GridViewWithHeaderAndFooter;
import in.srain.cube.views.IScrollHeaderFrameHandler;
import in.srain.cube.views.ScrollHeaderFrame;
import in.srain.cube.views.list.ListViewDataAdapter;
import in.srain.cube.views.list.ViewHolderBase;
import in.srain.cube.views.list.ViewHolderCreator;

public class GridListImageActivity extends TitleBaseActivity {

    private static final int sGirdImageSize = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(12 + 12 + 10)) / 2;
    private ImageLoader mImageLoader;

    private static final ImageReuseInfo sGridImageReuseInfo = Images.sImageReuseInfoManger.create("big_360");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageLoader = ImageLoaderFactory.create(this);
        setContentView(R.layout.activity_image_gird);
        final View v = mContainer;

        final GridViewWithHeaderAndFooter gridView = (GridViewWithHeaderAndFooter) v.findViewById(R.id.ly_image_list_grid);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View headerView = layoutInflater.inflate(R.layout.test_header_view, null);
        View footerView = layoutInflater.inflate(R.layout.test_footer_view, null);
        gridView.addHeaderView(headerView);
        gridView.addFooterView(footerView);

        final ListViewDataAdapter<JsonData> adapter = new ListViewDataAdapter<JsonData>(new ViewHolderCreator<JsonData>() {
            @Override
            public ViewHolderBase<JsonData> createViewHolder() {
                return new ViewHolder();
            }
        });
        gridView.setAdapter(adapter);
        setHeaderTitle("GridViewWithHeaderAndFooter");

        final PtrFrameDemo ptrFrame = (PtrFrameDemo) v.findViewById(R.id.ly_ptr_frame);
        ptrFrame.setKeepHeaderWhenRefresh(true);
        ptrFrame.setHandler(new PtrFrameDemo.DefaultHandler() {
            @Override
            public void onRefresh() {
                DemoRequestData.getImageList(false, new DemoRequestData.ImageListDataHandler() {

                    public void onData(JsonData data, CacheAbleRequest.ResultType type, boolean outOfDate) {
                        String msg = String.format(
                                " onData\n result type: %s\n out of date: %s\n time: %s",
                                type, outOfDate, data.optJson("data").optString("time"));
                        Toast.makeText(GridListImageActivity.this, msg, 1).show();
                        adapter.getDataList().clear();
                        adapter.getDataList().addAll(data.optJson("data").optJson("list").toArrayList());
                        adapter.notifyDataSetChanged();
                        ptrFrame.refreshComplete();
                    }
                });
            }
        });
        ptrFrame.postDelayed(new Runnable() {
            @Override
            public void run() {
                ptrFrame.doRefresh();
            }
        }, 150);
        ScrollHeaderFrame frame = (ScrollHeaderFrame) findViewById(R.id.scroll_header_frame);
        frame.setHandler(new IScrollHeaderFrameHandler() {
            @Override
            public boolean hasReachTop() {
                if (gridView.getChildCount() == 0) {
                    return true;
                }
                return gridView.getChildAt(0).getTop() == 0;
            }
        });
    }

    private class ViewHolder extends ViewHolderBase<JsonData> {

        private CubeImageView mImageView;

        @Override
        public View createView(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.item_image_list_grid, null);
            mImageView = (CubeImageView) view.findViewById(R.id.iv_item_iamge_list_grid);
            mImageView.setScaleType(ScaleType.CENTER_CROP);

            LinearLayout.LayoutParams lyp = new LinearLayout.LayoutParams(sGirdImageSize, sGirdImageSize);
            mImageView.setLayoutParams(lyp);
            return view;
        }

        @Override
        public void showData(int position, JsonData itemData) {
            if (position == 0) {
                int a = 0;
                if (a == 0) {

                }
            }
            String url = itemData.optString("pic");
            mImageView.loadImage(mImageLoader, url, sGridImageReuseInfo);
        }
    }
}
