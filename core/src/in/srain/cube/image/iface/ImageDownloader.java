package in.srain.cube.image.iface;

import java.io.InputStream;

public interface ImageDownloader {

    public InputStream download(String url);
}