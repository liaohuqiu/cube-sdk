package in.srain.cube.image.iface;

import android.graphics.BitmapFactory;
import in.srain.cube.image.ImageTask;

/**
 * A ImageReSizer process the resize logical when loading image from network an disk.
 */
public interface ImageReSizer {

    /**
     * Return the {@link BitmapFactory.Options#inSampleSize}, which will be used when load the image from the disk.
     * <p/>
     * You should better calculate this value according the hard device of the mobile.
     */
    public int getInSampleSize(ImageTask imageTask);

    public String getRemoteUrl(ImageTask imageTask);
}