package in.srain.cube.cache;

import org.json.JSONObject;

public class CacheInfo {

    public String data;
    public int time;
    public int mSize;

    public static CacheInfo create(String data) {
        CacheInfo info = new CacheInfo(data);
        info.time = (int) (System.currentTimeMillis() / 1000);
        return info;
    }

    public static CacheInfo create(String data, int time) {
        CacheInfo cacheInfo = new CacheInfo(data);
        cacheInfo.time = time;
        return cacheInfo;
    }

    private CacheInfo(String data) {
        this.data = data;
        mSize = (data.getBytes().length + 8);
    }

    public int getSize() {
        return mSize;
    }

    public String getCacheData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("time", time);
            jsonObject.put("data", data);
        } catch (Exception e) {
        }
        return jsonObject.toString();
    }
}
