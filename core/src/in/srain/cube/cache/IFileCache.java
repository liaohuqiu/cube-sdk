package in.srain.cube.cache;

public interface IFileCache {

    public String getCachePath();

    public long getUsedSpace();

    public void clearCache();

    public long getMaxSize();

    public boolean has(String key);
}
