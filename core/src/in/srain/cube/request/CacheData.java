package in.srain.cube.request;

import org.json.JSONObject;

import java.util.HashMap;

public class CacheData {

    public String data;
    public int time;
    public int mSize;

    public static CacheData create(String data) {
        CacheData cacheData = new CacheData(data);
        cacheData.time = (int) (System.currentTimeMillis() / 1000);
        return cacheData;
    }

    public static CacheData create(String data, int time) {
        CacheData cacheData = new CacheData(data);
        cacheData.time = time;
        return cacheData;
    }

    private CacheData(String data) {
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
