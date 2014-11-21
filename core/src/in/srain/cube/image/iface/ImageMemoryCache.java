package in.srain.cube.image.iface;

import android.graphics.drawable.BitmapDrawable;

public interface ImageMemoryCache {

    public void set(String key, BitmapDrawable data);

    public BitmapDrawable get(String key);

    public void clear();

    public void delete(String key);

    /**
     * max byte
     *
     * @return
     */
    public long getMaxSize();

    /**
     * used byte
     *
     * @return
     */
    public long getUsedSpace();
}
