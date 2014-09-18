package in.srain.cube.sample.app;

import android.app.Application;
import in.srain.cube.Cube;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.request.RequestCacheManager;
import in.srain.cube.sample.image.DemoDuiTangImageResizer;
import in.srain.cube.util.CLog;

public class CubeDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // other code
        // ..

        CLog.DEBUG_IMAGE = true;
        // CLog.DEBUG_SCROLL_HEADER_FRAME = true;
        // CLog.DEBUG_PTR_FRAME = true;
        CLog.DEBUG_REQUEST_CACHE = true;
        ImageLoaderFactory.setDefaultImageResizer(DemoDuiTangImageResizer.getInstance());
        String dir = "request-cache";
        RequestCacheManager.getInstance().init(this, dir);
        Cube.onCreate(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // other code
        // ...

        Cube.onTerminate();
    }
}