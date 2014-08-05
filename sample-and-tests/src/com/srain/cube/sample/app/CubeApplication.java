package net.liaohuqiu.cube.sample.app;

import android.app.Application;

import net.liaohuqiu.cube.Cube;
import net.liaohuqiu.cube.util.CLog;

public class CubeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // other code
        // ..

        CLog.DEBUG_IMAGE = true;
        CLog.DEBUG_SCROLL_HEADER_FRAME = true;
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