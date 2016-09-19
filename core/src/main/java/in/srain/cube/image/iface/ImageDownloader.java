package in.srain.cube.image.iface;

import in.srain.cube.image.ImageTask;

import java.io.OutputStream;

public interface ImageDownloader {

    public boolean downloadToStream(ImageTask imageTask,
                                 String url,
                                 OutputStream outputStream,
                                 ProgressUpdateHandler progressUpdateHandler);
}