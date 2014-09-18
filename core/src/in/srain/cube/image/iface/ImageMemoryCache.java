package in.srain.cube.image.iface;

import android.graphics.drawable.BitmapDrawable;

public interface ImageMemoryCache {

    public void set(String key, BitmapDrawable data);

    public BitmapDrawable get(String key);

    public void clear();

    public void delete(String key);

    public int getMaxSize();

    public int getUsedSpace();
}
