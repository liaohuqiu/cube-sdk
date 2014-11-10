package in.srain.cube.sample.activity;

import android.os.Bundle;
import android.view.View;
import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.base.TitleBaseActivity;
import in.srain.cube.sample.ui.views.StoreHouseHeader;
import in.srain.cube.util.CLog;
import in.srain.cube.util.Debug;
import in.srain.cube.views.ptr.PtrFrame;

public class PtrStoreHouseHeaderActivity extends TitleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_house_ptr_header);

        setHeaderTitle("Storehouse");

        CubeImageView imageView = (CubeImageView) findViewById(R.id.store_house_ptr_image);
        ImageLoader imageLoader = ImageLoader.createDefault(this);
        String pic = "http://img5.duitang.com/uploads/item/201406/28/20140628122218_fLQyP.thumb.jpeg";
        imageView.loadImage(imageLoader, pic);

        Debug.DEBUG_PTR_FRAME = true;
        final PtrFrame frame = (PtrFrame) findViewById(R.id.ly_ptr_frame);
        final StoreHouseHeader houseHeader = (StoreHouseHeader) findViewById(R.id.store_house_ptr_header);
        frame.setPtrHandler(new PtrFrame.DefaultPtrHandler() {

            @Override
            public boolean checkCanDoRefresh(PtrFrame frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefresh() {
                houseHeader.beginLoading();
                CLog.d("ptr-test", "onRefresh");
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        frame.refreshComplete();
                    }
                }, 5000);
            }

            @Override
            public void onBackToTop() {
                CLog.d("ptr-test", "onBackToTop");
            }

            @Override
            public void onRelease() {
                CLog.d("ptr-test", "onRelease");

            }

            @Override
            public void onRefreshComplete() {
                CLog.d("ptr-test", "onRefreshComplete");
                houseHeader.loadFinish();
            }

            @Override
            public void crossRotateLineFromTop(boolean isInTouching) {
                CLog.d("ptr-test", "crossRotateLineFromTop");
            }

            @Override
            public void onPercentageChange(int oldPosition, int newPosition, float oldPercent, float newPercent) {
                float f = newPosition * 1f / houseHeader.getMeasuredHeight();
                if (f > 1) f = 1;
                // CLog.d("ptr-test", "onPercentageChange: %s %s", newPosition, houseHeader.getMeasuredHeight(), houseHeader.getHeight());
                houseHeader.setProgress(f);
                houseHeader.invalidate();
            }

            @Override
            public void crossRotateLineFromBottom(boolean isInTouching) {
                CLog.d("ptr-test", "crossRotateLineFromBottom");
            }
        });
    }
}
