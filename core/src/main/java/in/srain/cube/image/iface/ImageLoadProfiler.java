package in.srain.cube.image.iface;

import in.srain.cube.image.ImageTask;
import in.srain.cube.image.ImageTaskStatistics;

public interface ImageLoadProfiler {
    public void onImageLoaded(ImageTask imageTask, ImageTaskStatistics stat);
}
