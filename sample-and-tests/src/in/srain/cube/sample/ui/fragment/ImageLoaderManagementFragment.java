package in.srain.cube.sample.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.image.ImageProvider;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.TitleBaseFragment;
import in.srain.cube.sample.ui.views.header.TitleAndValue;

public class ImageLoaderManagementFragment extends TitleBaseFragment {

    private TitleAndValue mFileCachePath;
    private TitleAndValue mFileCacheMax;
    private TitleAndValue mFileCacheUsed;
    private TitleAndValue mMemoryCacheMax;
    private TitleAndValue mMemoryCacheUsed;

    private ImageLoader mImageLoader;
    private LinearLayout mList;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHeaderTitle("ImageLoader Management");

        View view = inflater.inflate(R.layout.fragment_imageloader_management, null);
        mList = (LinearLayout) view.findViewById(R.id.ly_btn_image_loader_management);

        mFileCachePath = addTitleAndValue("file cache path:");
        mFileCacheMax = addTitleAndValue("file cache max:");
        mFileCacheUsed = addTitleAndValue("file cache used:");

        mMemoryCacheMax = addTitleAndValue("memory max:");
        mMemoryCacheUsed = addTitleAndValue("memory used:");

        mImageLoader = ImageLoaderFactory.create(getActivity());

        view.findViewById(R.id.btn_image_loader_management_clear_cache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImageLoader.getImageProvider().clearDiskCache();
                mImageLoader.getImageProvider().clearMemoryCache();
                update();
            }
        });

        update();
        return view;
    }

    private TitleAndValue addTitleAndValue(String title) {
        TitleAndValue tv = new TitleAndValue(getContext());
        tv.title(title);
        mList.addView(tv);
        return tv;
    }

    private void update() {

        mFileCachePath.value(mImageLoader.getImageProvider().getFileCachePath());
        mFileCacheMax.value(mImageLoader.getImageProvider().getFileCacheMaxSpace() / 1024f / 1024 + "MB");
        mFileCacheUsed.value(mImageLoader.getImageProvider().getFileCacheUsedSpace() / 1024f / 1024 + "MB");

        mMemoryCacheMax.value(mImageLoader.getImageProvider().getMemoryCacheMaxSpace() / 1024f / 1024 + "MB");
        mMemoryCacheUsed.value(mImageLoader.getImageProvider().getMemoryCacheUsedSpace() / 1024f / 1024 + "MB");
    }
}