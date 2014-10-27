package in.srain.cube.sample.activity.imagelist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.ImageReuseInfo;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.base.TitleBaseActivity;
import in.srain.cube.sample.data.Images;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.list.ListViewDataAdapter;
import in.srain.cube.views.list.ViewHolderBase;
import in.srain.cube.views.list.ViewHolderCreator;

import java.util.Arrays;

public class BigImageListActivity extends TitleBaseActivity {

    private static final int sBigImageSize = LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(10 + 10);

    private ImageLoader mImageLoader;
    private static final ImageReuseInfo sBigImageReuseInfo = Images.sImageReuseInfoManger.create("big");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHeaderTitle("Big Image");
        setContentView(R.layout.activity_image_list_big);
        mImageLoader = ImageLoaderFactory.create(this);

        final View v = mContainer;
        ListView listView = (ListView) v.findViewById(R.id.ly_image_list_big);
        ListViewDataAdapter<String> adapter = new ListViewDataAdapter<String>(new ViewHolderCreator<String>() {
            @Override
            public ViewHolderBase<String> createViewHolder() {
                return new ViewHolder();
            }
        });
        listView.setAdapter(adapter);
        adapter.getDataList().addAll(Arrays.asList(Images.imageUrls));
        adapter.notifyDataSetChanged();
    }

    private class ViewHolder extends ViewHolderBase<String> {

        private CubeImageView mImageView;

        @Override
        public View createView(LayoutInflater inflater) {
            View view = inflater.inflate(R.layout.item_image_list_big, null);
            mImageView = (CubeImageView) view.findViewById(R.id.iv_item_image_list_big);
            mImageView.setScaleType(ScaleType.CENTER_CROP);

            LinearLayout.LayoutParams lyp = new LinearLayout.LayoutParams(sBigImageSize, sBigImageSize);
            mImageView.setLayoutParams(lyp);

            return view;
        }

        @Override
        public void showData(int position, String itemData) {
            mImageView.loadImage(mImageLoader, itemData, sBigImageReuseInfo);
        }
    }
}
