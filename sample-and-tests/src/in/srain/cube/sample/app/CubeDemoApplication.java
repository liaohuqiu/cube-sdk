package in.srain.cube.sample.app;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import in.srain.cube.Cube;
import in.srain.cube.image.ImageLoaderFactory;
import in.srain.cube.request.RequestCacheManager;
import in.srain.cube.sample.image.DemoDuiTangImageResizer;
import in.srain.cube.util.CLog;

public class CubeDemoApplication extends Application {

    public static CubeDemoApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        CLog.DEBUG_IMAGE = true;
        CLog.DEBUG_CACHE = true;

        CLog.DEBUG_SCROLL_HEADER_FRAME = true;
        // CLog.DEBUG_PTR_FRAME = true;
        ImageLoaderFactory.setDefaultImageResizer(DemoDuiTangImageResizer.getInstance());
        String dir = "request-cache";
        RequestCacheManager.init(this, dir, 1024 * 10, 1024 * 10);
        Cube.onCreate(this);

        try {
            ActivityInfo[] list = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES).activities;

            for (int i = 0; i < list.length; i++) {
                System.out.println("List of running activities" + list[i].name);

            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public void test() {
        CLog.d("test", "info: %s", this);
    }
}