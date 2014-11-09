package in.srain.cube.sample.image;

import android.content.Context;
import in.srain.cube.app.lifecycle.LifeCycleComponent;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.ImageProvider;
import in.srain.cube.image.iface.ImageLoadHandler;
import in.srain.cube.image.iface.ImageResizer;
import in.srain.cube.image.iface.ImageTaskExecutor;

public class DemoImageLoader extends ImageLoader implements LifeCycleComponent {

    public static ImageLoader createStableImageLoader(Context context) {
        return null;
    }

    public DemoImageLoader(Context context, ImageProvider imageProvider, ImageTaskExecutor imageTaskExecutor, ImageResizer imageResizer, ImageLoadHandler imageLoadHandler) {
        super(context, imageProvider, imageTaskExecutor, imageResizer, imageLoadHandler);
    }

    @Override
    public void onRestart() {
        recoverWork();
    }

    @Override
    public void onPause() {
        pauseWork();
    }

    @Override
    public void onResume() {
        resumeWork();
    }

    @Override
    public void onStop() {
        stopWork();
    }

    @Override
    public void onDestroy() {
        destroy();
    }
}
