package in.srain.cube.sample.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.request.RequestCacheManager;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.TitleBaseFragment;
import in.srain.cube.sample.ui.views.header.TitleAndValue;

public class RequestCacheManagementFragment extends TitleBaseFragment {

    private TitleAndValue mFileCachePath;
    private TitleAndValue mFileCacheMax;
    private TitleAndValue mFileCacheUsed;
    private TitleAndValue mMemoryCacheMax;
    private TitleAndValue mMemoryCacheUsed;

    private RequestCacheManager mRequestCacheManager;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHeaderTitle("ImageLoader Management");

        View view = inflater.inflate(R.layout.fragment_request_cache_management, null);

        mRequestCacheManager = RequestCacheManager.getInstance();


        view.findViewById(R.id.btn_image_loader_management_clear_cache).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mRequestCacheManager.clearDiskCache();
                update();
            }
        });

        update();
        return view;
    }

    private void update() {

        mFileCachePath.value(mRequestCacheManager.getFileCachePath());
        mFileCacheMax.value(mRequestCacheManager.getFileCacheMaxSpace() / 1024f / 1024 + "MB");
        mFileCacheUsed.value(mRequestCacheManager.getFileCacheUsedSpace() / 1024f / 1024 + "MB");
    }
}