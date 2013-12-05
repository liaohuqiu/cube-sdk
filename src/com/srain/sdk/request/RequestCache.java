package com.srain.sdk.request;

import java.io.File;
import java.util.HashMap;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.srain.sdk.Cube;
import com.srain.sdk.file.FileUtil;

/**
 * 
 * @author huqiu.lhq
 */
public class RequestCache {

	private static RequestCache mInstance;

	private String mCacheDir;

	private HashMap<String, JsonData> mCacheList;

	private static final int REQUEST_CACHE_SUCC = 0x01;
	private static final int REQUEST_ASSERT_CACHE_SUCC = 0x02;

	public interface ICacheable {

		public int getCacheTime();

		public String getCacheKey();

		public String getAssertInitDataPath();

		/**
		 * We need to process the data from data source, do some filter of convert the structure.
		 * 
		 * As the "Assert Data" is a special data souce, we also need to do the same work.
		 */
		public JsonData processDataFromAssert(JsonData jsonData);

		public void onNoCacheDataAvailable();

		public void onCacheData(JsonData previousJsonData, boolean outofDate);
	}

	private RequestCache() {
		String specifiedPathInSDCard = Cube.getInstance().getRootDirNameInSDCard();
		mCacheDir = FileUtil.wantFilesPath(Cube.getInstance().getContext(), true, specifiedPathInSDCard) + "/request";
		mCacheList = new HashMap<String, JsonData>();
		showStatus(String.format("init, cache dir::%s", mCacheDir));
	}

	public static RequestCache getInstance() {
		if (null == mInstance)
			mInstance = new RequestCache();
		return mInstance;
	}

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
		showStatus(String.format("invalidateCache, key:%s", key));
		String filePath = mCacheDir + "/ " + key;
		new File(filePath).delete();
		mCacheList.remove(key);
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
