package in.srain.cube.cache;

import java.io.InputStream;

public interface IFileCache {

    public String getCachePath();

    public long getUsedSpace();

    public void clearCache();

    public int getMaxSize();

    public boolean has(String key);
}
