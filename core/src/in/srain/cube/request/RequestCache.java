package in.srain.cube.request;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import in.srain.cube.concurrent.SimpleExcutor;
import in.srain.cube.file.FileUtil;
import in.srain.cube.util.CLog;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

/**
 * @author http://www.liaohuqiu.net
 */
public class RequestCache {

    private static final boolean DEBUG = CLog.DEBUG_REQUEST_CACHE;
    private static final String LOG_TAG = "cube_request_cache";

    private static final String THREAD_NAME = "Cube-Request-Cache";

    private static RequestCache mInstance;
    private String mCacheDir;

    private HashMap<String, JsonData> mCacheList;

    private static final int AFTER_READ_FROM_FILE = 0x01;
    private static final int AFTER_READ_FROM_ASSERT = 0x02;
    private static final int AFTER_CONVERT = 0x04;

    private static final int DO_READ_FROM_FILE = 0x01;
    private static final int DO_READ_FROM_ASSERT = 0x02;
    private static final int DO_CONVERT = 0x04;

    private Context mContext;
    private static Handler sHandler;

    public interface ICacheAbleRequest<T> extends IRequest<T> {

        public int getCacheTime();

        public String getCacheKey();

        public String getAssertInitDataPath();

        /**
         * We need to process the data from data source, do some filter of convert the structure.
         * <p/>
         * As the "Assert Data" is a special data source, we also need to do the same work.
         */
        public T processRawDataFromCache(JsonData jsonData);

        public void onCacheData(T previousJsonData, boolean outOfDate);

        public void queryFromServer();

        public boolean disableCache();
    }

    @SuppressLint("HandlerLeak")
    private RequestCache() {

        sHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                ReadCacheTask<?> task = null;
                switch (msg.what) {
                    case AFTER_READ_FROM_FILE:
                    case AFTER_READ_FROM_ASSERT:
                        task = (ReadCacheTask<?>) msg.obj;
                        task.processRawCacheData();
                        break;

                    case AFTER_CONVERT:
                        task = (ReadCacheTask<?>) msg.obj;
                        task.done();
                        break;

                    default:
                        break;
                }
            }
        };
    }

    public void init(Context content, String cacheDir) {
        mContext = content;
        mCacheDir = cacheDir;
        mCacheList = new HashMap<String, JsonData>();
        if (DEBUG) {
            CLog.d(LOG_TAG, "init, cache dir::%s", mCacheDir);
        }
    }

    public static RequestCache getInstance() {
        if (null == mInstance)
            mInstance = new RequestCache();
        return mInstance;
    }

    public <T> void requestCache(ICacheAbleRequest<T> cacheAbleRequest) {
        ReadCacheTask<T> task = new ReadCacheTask<T>(cacheAbleRequest);
        task.beginQuery();
    }

    public <T> void cacheRequest(final ICacheAbleRequest<T> cacheAble, final JsonData data) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "cacheRequest, key:%s", cacheAble.getCacheKey());
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String filePath = mCacheDir + "/ " + cacheAble.getCacheKey();
                JsonData jsonData = makeCacheFormatJsonData(data, 0);
                setCacheData(cacheAble.getCacheKey(), jsonData);
                FileUtil.write(filePath, jsonData.toString());
            }
        }, THREAD_NAME).start();
    }

    private JsonData makeCacheFormatJsonData(JsonData data, int time) {
        JSONObject jsonObject = new JSONObject();
        if (time == 0)
            time = (int) (System.currentTimeMillis() / 1000);
        try {
            jsonObject.put("time", time);
            jsonObject.put("data", data.getRawData());
        } catch (Exception e) {
        }
        return JsonData.create(jsonObject);
    }

    public void invalidateCache(String key) {
        if (DEBUG) {
            CLog.d(LOG_TAG, "invalidateCache, key:%s", key);
        }
        String filePath = mCacheDir + "/ " + key;
        new File(filePath).delete();
        mCacheList.remove(key);
    }

    private class ReadCacheTask<T1> implements Runnable {

        private ICacheAbleRequest<T1> mCacheAble;

        private JsonData mRawData;
        private T1 mResult;
        private int mWorkType = 0;

        public ReadCacheTask(ICacheAbleRequest<T1> cacheAble) {
            mCacheAble = cacheAble;
        }

        void beginQuery() {

            if (mCacheAble.disableCache()) {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "Cache is disabled, query from server, key:%s", mCacheAble.getCacheKey());
                }
                mCacheAble.queryFromServer();
                return;
            }

            String cacheKey = mCacheAble.getCacheKey();

            // try to find in runtime cache
            if (mCacheList.containsKey(cacheKey)) {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "exist in list, key:%s", mCacheAble.getCacheKey());
                }
                mRawData = mCacheList.get(cacheKey);
                processRawCacheData();
                return;
            }

            // try read from cache data
            String filePath = mCacheDir + "/ " + mCacheAble.getCacheKey();
            File file = new File(filePath);
            if (file.exists()) {
                queryFromCacheFileAsync();
                return;
            }

            // try to read from assert cache file
            String assertInitDataPath = mCacheAble.getAssertInitDataPath();
            if (assertInitDataPath != null && assertInitDataPath.length() > 0) {
                queryFromAssertCacheFileAsync();
                return;
            }

            if (DEBUG) {
                CLog.d(LOG_TAG, "cache file not exist, key:%s", mCacheAble.getCacheKey());
            }
            mCacheAble.queryFromServer();
        }

        @Override
        public void run() {

            switch (mWorkType) {

                case DO_READ_FROM_FILE:
                    doQueryFromCacheFileInBackground();
                    break;

                case DO_READ_FROM_ASSERT:
                    queryFromAssertCacheFileInBackground();
                    break;

                case DO_CONVERT:
                    doConvertInBackground();
                    break;

                default:
                    break;
            }
        }

        private void queryFromCacheFileAsync() {
            mWorkType = DO_READ_FROM_FILE;
            SimpleExcutor.getInstance().execute(this);
        }

        private void doQueryFromCacheFileInBackground() {

            if (DEBUG) {
                CLog.d(LOG_TAG, "try read cache data from file, key:%s", mCacheAble.getCacheKey());
            }
            String filePath = mCacheDir + "/ " + mCacheAble.getCacheKey();
            String cacheContent = FileUtil.read(filePath);
            mRawData = JsonData.create(cacheContent);

            Message msg = Message.obtain();
            msg.what = AFTER_READ_FROM_FILE;
            msg.obj = this;
            sHandler.sendMessage(msg);
        }

        private void queryFromAssertCacheFileAsync() {
            mWorkType = DO_READ_FROM_ASSERT;
            SimpleExcutor.getInstance().execute(this);
        }

        private void queryFromAssertCacheFileInBackground() {

            if (DEBUG) {
                CLog.d(LOG_TAG, "try read cache data from assert file: %s", mCacheAble.getCacheKey());
            }

            String cacheContent = FileUtil.readAssert(mContext, mCacheAble.getAssertInitDataPath());
            JsonData rawData = JsonData.create(cacheContent);
            mRawData = makeCacheFormatJsonData(rawData, -2);
            setCacheData(mCacheAble.getCacheKey(), mRawData);

            Message msg = Message.obtain();
            msg.what = AFTER_READ_FROM_ASSERT;
            msg.obj = this;
            sHandler.sendMessage(msg);
        }

        void doConvertInBackground() {

            JsonData data = mRawData.optJson("data");
            mResult = mCacheAble.processRawDataFromCache(data);

            Message msg = Message.obtain();
            msg.what = AFTER_CONVERT;
            msg.obj = this;
            sHandler.sendMessage(msg);
        }

        private void processRawCacheData() {
            if (null != mRawData && mRawData.has("data")) {
                mWorkType = DO_CONVERT;
                SimpleExcutor.getInstance().execute(this);
            } else {
                if (DEBUG) {
                    CLog.d(LOG_TAG, "onNoCacheDataAvailable, key:%s", mCacheAble.getCacheKey());
                }
                mCacheAble.queryFromServer();
            }
        }

        void done() {

            int lastTime = mRawData.optInt("time");
            long timeInterval = System.currentTimeMillis() / 1000 - lastTime;
            boolean outOfDate = timeInterval > mCacheAble.getCacheTime() || timeInterval < 0;
            mCacheAble.onCacheData(mResult, outOfDate);
            if (outOfDate) {
                mCacheAble.queryFromServer();
            }
        }
    }

    private void setCacheData(String key, JsonData data) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (DEBUG) {
            CLog.d(LOG_TAG, "set cache to runtime cache list, key:%s", key);
        }
        mCacheList.put(key, data);
    }
}