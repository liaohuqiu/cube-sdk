package in.srain.cube.image.iface;

import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageTask;

/**
 * Define load progress
 */
public interface ImageLoadProgressHandler {

    void onProgressChange(ImageTask imageTask, CubeImageView cubeImageView, int loaded, int total);
}
