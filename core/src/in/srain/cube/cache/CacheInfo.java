package in.srain.cube.cache;

import in.srain.cube.request.JsonData;
import org.json.JSONObject;

/**
 * A description for the data in cache system
 */
public class CacheInfo {

    private String mData;
    private long mTime;
    private int mSize;

    public static CacheInfo createForNow(String data) {
        CacheInfo info = new CacheInfo(data);
        info.mTime = (int) (System.currentTimeMillis() / 1000);
        return info;
    }

    public static CacheInfo createInvalidated(String data) {
        return create(data, -2);
    }

    public static CacheInfo createFromJson(JsonData jsonData) {
        return create(jsonData.optString("data"), jsonData.optInt("time"));
    }

    private static CacheInfo create(String data, long time) {
        CacheInfo cacheInfo = new CacheInfo(data);
        cacheInfo.mTime = time;
        return cacheInfo;
    }

    private CacheInfo(String data) {
        this.mData = data;
        mSize = (data.getBytes().length + 8);
    }

    public int getSize() {
        return mSize;
    }

    public long getTime() {
        return mTime;
    }

    public String getData() {
        return mData;
    }

    public String getCacheData() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("time", mTime);
            jsonObject.put("data", mData);
        } catch (Exception e) {
        }
        return jsonObject.toString();
    }
}
