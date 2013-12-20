package com.srain.cube.request;

import java.io.File;
import java.util.HashMap;

import org.json.JSONObject;

import com.srain.cube.Cube;
import com.srain.cube.file.FileUtil;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * <ul>
 * <li>
 * Try to find cache data in runtime hashtable, cache file, init data file in assert.</li>
 * 
 * <li>If cache data is exist, onCacheData will be called,
 * 
 * with a parameter expired which indicates wheathe the data is expired.</li>
 * 
 * <li>If the data is cached and is no expired, not request will be sent out.</li>
 * </ul>
 * 
 * @author huqiu.lhq
 */
public class RequestCacheManager {

	private static RequestCacheManager mInstance;

	private String mCacheDir;

	private HashMap<String, JsonData> mCacheList;

	private static final int REQUEST_CACHE_SUCC = 0x01;
	private static final int REQUEST_ASSERT_CACHE_SUCC = 0x02;

	public interface ICacheable {

		/**
		 * Indicates how long the data will be expired. In seconds.
		 */
		public int getCacheTime();

		/**
		 * Indicates the key by which the data is cached.
		 */
		public String getCacheKey();

		/**
		 * Specify the path of the initial data file.
		 */
		public String getAssertInitDataPath();

		/**
		 * We need to process the data from data source, do some filter of convert the structure.
		 * 
		 * As the "Assert Data" is a special data souce, we also need to do the same work.
		 */
		public JsonData processDataFromAssert(JsonData jsonData);

		/**
		 * When no data in file cache, neither initial data in assert file.
		 */
		public void onNoCacheDataAvailable();

		/**
		 * When cache data read from file cache or assert file.
		 * 
		 * @param cacheData
		 * @param outofDate
		 *            indicates whether the data is expired.
		 */
		public void onCacheData(JsonData cacheData, boolean outofDate);
	}

	private RequestCacheManager() {
		String specifiedPathInSDCard = Cube.getInstance().getRootDirNameInSDCard();
		mCacheDir = FileUtil.wantFilesPath(Cube.getInstance().getContext(), true, specifiedPathInSDCard) + "/request";
		mCacheList = new HashMap<String, JsonData>();
		showStatus(String.format("init, cache dir::%s", mCacheDir));
	}

	public static RequestCacheManager getInstance() {
		if (null == mInstance)
			mInstance = new RequestCacheManager();
		return mInstance;
	}

	/**
	 * Try to find out if there is any cache data. If cache data is exist, onCacheData method will be called.
	 * 
	 * Or else onNoCacheDataAvailable.
	 */
	public void requestCache(ICacheable cacheable) {

		String cacheKey = cacheable.getCacheKey();

		// try to find in runtime cache
		if (mCacheList.containsKey(cacheKey)) {
			showStatus(String.format("exsit in list, key:%s", cacheable.getCacheKey()));
			processCacheData(mCacheList.get(cacheKey), cacheable);
			return;
		}

		// try read from cache data
		String filePath = mCacheDir + "/ " + cacheable.getCacheKey();
		File file = new File(filePath);
		if (file.exists()) {
			queryFromCacheFile(cacheable);
			return;
		}

		// try to read from asser cache file
		String assertInitDataPath = cacheable.getAssertInitDataPath();
		if (assertInitDataPath != null && assertInitDataPath.length() > 0) {
			queryFromAssertCacheFile(cacheable);
			return;
		}

		showStatus(String.format("cache file not exist, key:%s", cacheable.getCacheKey()));
		processCacheData(null, cacheable);
	}

	/**
	 * Cache a cacheable request, store it's data in a runtime Hashtable,
	 * 
	 * and write the persistent data to file, whose filename is the cache key.
	 */
	public void cacheRequest(final ICacheable cacheable, final JsonData data) {
		showStatus(String.format("cacheRequest, key:%s", cacheable.getCacheKey()));
		new Thread(new Runnable() {
			@Override
			public void run() {
				String filePath = mCacheDir + "/ " + cacheable.getCacheKey();
				JsonData jsonData = makeCacheFormatJsonData(data, 0);
				setCacheData(cacheable.getCacheKey(), jsonData);
				FileUtil.write(filePath, jsonData.toString());
			}
		}, "Request-Cache").start();
	}

	/**
	 * Invalidate the cache data by the given out key.
	 */
	public void invalidateCache(String key) {
		showStatus(String.format("invalidateCache, key:%s", key));
		String filePath = mCacheDir + "/ " + key;
		new File(filePath).delete();
		mCacheList.remove(key);
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

	private void queryFromCacheFile(final ICacheable cacheable) {

		// no in main thread, will no cause HandlerLeak
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REQUEST_CACHE_SUCC:
					JsonData cacheData = (JsonData) msg.obj;
					setCacheData(cacheable.getCacheKey(), cacheData);
					processCacheData(cacheData, cacheable);
					break;

				default:
					break;
				}
			}
		};

		new Thread(new Runnable() {
			@Override
			public void run() {
				showStatus(String.format("try read cache data from file, key:%s", cacheable.getCacheKey()));
				String filePath = mCacheDir + "/ " + cacheable.getCacheKey();
				String cacheContent = FileUtil.read(filePath);
				JsonData cacheData = JsonData.create(cacheContent);

				Message msg = Message.obtain();
				msg.what = REQUEST_CACHE_SUCC;
				msg.obj = cacheData;
				handler.sendMessage(msg);
			}
		}, "Request-Cache").start();
	}

	@SuppressLint("HandlerLeak")
	private void queryFromAssertCacheFile(final ICacheable cacheable) {

		// no in main thread, will no cause HandlerLeak
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REQUEST_ASSERT_CACHE_SUCC:
					JsonData data = (JsonData) msg.obj;
					JsonData cacheData = makeCacheFormatJsonData(data, -2);
					setCacheData(cacheable.getCacheKey(), cacheData);
					processCacheData(cacheData, cacheable);
					break;

				default:
					break;
				}
			}
		};

		new Thread(new Runnable() {
			@Override
			public void run() {
				showStatus(String.format("try read cache data from assert file: %s", cacheable.getCacheKey()));
				String cacheContent = FileUtil.readAssert(Cube.getInstance().getContext(), cacheable.getAssertInitDataPath());
				JsonData cacheData = JsonData.create(cacheContent);
				cacheData = cacheable.processDataFromAssert(cacheData);

				Message msg = Message.obtain();
				msg.what = REQUEST_ASSERT_CACHE_SUCC;
				msg.obj = cacheData;
				handler.sendMessage(msg);
			}
		}, "Request-Cache").start();
	}

	private void processCacheData(JsonData cacheData, ICacheable cacheable) {

		if (null != cacheData && cacheData.has("data")) {
			int lastTime = cacheData.optInt("time");
			JsonData data = cacheData.optJson("data");
			boolean outofDate = System.currentTimeMillis() / 1000 - lastTime > cacheable.getCacheTime();
			cacheable.onCacheData(data, outofDate);
		} else {

			showStatus(String.format("onNoCacheDataAvailable, key:%s", cacheable.getCacheKey()));
			cacheable.onNoCacheDataAvailable();
		}
	}

	private void setCacheData(String key, JsonData data) {
		showStatus(String.format("set cache to runtime cache list, key:%s", key));
		mCacheList.put(key, data);
	}

	private void showStatus(String msg) {
		Log.d("cube_request", msg);
	}
}
