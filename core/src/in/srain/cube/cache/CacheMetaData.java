package in.srain.cube.cache;

import in.srain.cube.request.JsonData;
import org.json.JSONObject;

/**
 * A description for the data in cache system
 */
public class CacheMetaData {

    private String mData;
    private long mTime;
    private int mSize;

    public static CacheMetaData createForNow(String data) {
        CacheMetaData info = new CacheMetaData(data);
        info.mTime = (int) (System.currentTimeMillis() / 1000);
        return info;
    }

    public static CacheMetaData createInvalidated(String data) {
        return create(data, -2);
    }

    public static CacheMetaData createFromJson(JsonData jsonData) {
        return create(jsonData.optString("data"), jsonData.optInt("time"));
    }

    private static CacheMetaData create(String data, long time) {
        CacheMetaData cacheMetaData = new CacheMetaData(data);
        cacheMetaData.mTime = time;
        return cacheMetaData;
    }

    private CacheMetaData(String data) {
        this.mData = data;
        mSize = (data.getBytes().length + 8);
    }

    public int getSize() {
        return mSize;
    }

    public long getTime() {
        return mTime;
    }

    public boolean isOutOfDateFor(ICacheAble<?> cacheAble) {
        long lastTime = getTime();
        long timeInterval = System.currentTimeMillis() / 1000 - lastTime;
        boolean outOfDate = timeInterval > cacheAble.getCacheTime() || timeInterval < 0;
        return outOfDate;
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
