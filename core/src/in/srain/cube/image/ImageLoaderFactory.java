package in.srain.cube.image;

import android.content.Context;
import in.srain.cube.image.iface.ImageLoadHandler;
import in.srain.cube.image.iface.ImageResizer;
import in.srain.cube.image.iface.ImageTaskExecutor;
import in.srain.cube.image.impl.DefaultImageLoadHandler;
import in.srain.cube.image.impl.DefaultImageResizer;
import in.srain.cube.image.impl.DefaultImageTaskExecutor;

/**
 * Manager the ImageTask loading list,
 *
 * @author http://www.liaohuqiu.net
 */
public class ImageLoaderFactory {

    private static ImageResizer sDefaultImageResizer;
    private static ImageTaskExecutor sDefaultImageTaskExecutor;
    private static ImageLoadHandler sDefaultImageLoadHandler;
    private static ImageProvider sDefaultImageProvider;

    public static ImageLoader create(Context context) {
        ImageLoader imageLoader = create(context, sDefaultImageProvider, sDefaultImageTaskExecutor, sDefaultImageResizer, sDefaultImageLoadHandler);
        return imageLoader;
    }

    public static ImageLoader create(Context context, ImageProvider imageProvider, ImageTaskExecutor imageTaskExecutor, ImageResizer imageResizer, ImageLoadHandler imageLoadHandler) {

        if (imageProvider == null) {
            imageProvider = ImageProvider.getDefault(context);
        }

        if (imageTaskExecutor == null) {
            imageTaskExecutor = DefaultImageTaskExecutor.getInstance();
        }

        if (imageResizer == null) {
            imageResizer = DefaultImageResizer.getInstance();
        }

        if (imageLoadHandler == null) {
            imageLoadHandler = new DefaultImageLoadHandler(context);
        }
        return new ImageLoader(context, imageProvider, imageTaskExecutor, imageResizer, imageLoadHandler);
    }

    public static void setDefaultImageResizer(ImageResizer imageResizer) {
        sDefaultImageResizer = imageResizer;
    }

    public static void setDefaultImageTaskExecutor(ImageTaskExecutor imageTaskExecutor) {
        sDefaultImageTaskExecutor = imageTaskExecutor;
    }

    public static void setDefaultImageLoadHandler(ImageLoadHandler imageLoadHandler) {
        sDefaultImageLoadHandler = imageLoadHandler;
    }

    public static void setDefaultImageProvider(ImageProvider imageProvider) {
        sDefaultImageProvider = imageProvider;
    }
}
