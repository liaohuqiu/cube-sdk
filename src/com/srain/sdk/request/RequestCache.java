package com.srain.sdk.request;

import java.io.File;
import java.util.HashMap;

import org.json.JSONObject;

import com.srain.sdk.Cube;
import com.srain.sdk.file.FileUtil;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

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

	public interface Cacheable {

		public int getCacheTime();

		public String getCacheKey();

		public String getAssertInitDataPath();

		public void onNoCacheDataAvailable();

		public void onCacheData(JsonData cacheJsonData);

		public void onCachedPreviousData(JsonData previousJsonData);
	}

	private RequestCache() {
		String specifiedPathInSDCard = Cube.getInstance().getRootDirNameInSDCard();
		mCacheDir = FileUtil.wantFilesPath(Cube.getInstance().getContext(), true, specifiedPathInSDCard) + "/request";
		mCacheList = new HashMap<String, JsonData>();
		showStatus("init", "cache dir: " + mCacheDir);
	}

	public static RequestCache getInstance() {
		if (null == mInstance)
			mInstance = new RequestCache();
		return mInstance;
	}

	public void requestCache(Cacheable cacheable) {

		String cacheKey = cacheable.getCacheKey();

		if (mCacheList.containsKey(cacheKey)) {
			showStatus(cacheable.getCacheKey(), "exsit in list");
			processCacheData(mCacheList.get(cacheKey), cacheable);
			return;
		}

		// read from cache data
//		String filePath = mCacheDir + "/ " + cacheable.getCacheKey();
//		File file = new File(filePath);
//		if (file.exists()) {
//			queryFromCacheFile(cacheable);
//			return;
//		}

		// try to read from asser cache file
		String assertInitDataPath = cacheable.getAssertInitDataPath();
		if (assertInitDataPath != null && assertInitDataPath.length() > 0) {
			queryFromAssertCacheFile(cacheable);
			return;
		}

		showStatus(cacheable.getCacheKey(), "cache file not exist");
		processCacheData(null, cacheable);
	}

	public void cacheRequest(final Cacheable cacheable, final JsonData data) {

		showStatus(cacheable.getCacheKey(), "cacheRequest");
		new Thread(new Runnable() {
			@Override
			public void run() {
				String filePath = mCacheDir + "/ " + cacheable.getCacheKey();
				JSONObject jsonObject = new JSONObject();
				int now = (int) (System.currentTimeMillis() / 1000);
				try {
					jsonObject.put("time", now);
					jsonObject.put("data", data.getRawData());
				} catch (Exception e) {
				}

				setCacheData(cacheable.getCacheKey(), JsonData.create(jsonObject));
				FileUtil.write(filePath, jsonObject.toString());
			}
		}).start();
	}

	public void invalidateCache(String key) {
		showStatus(key, "invalidateCache");

		String filePath = mCacheDir + "/ " + key;
		new File(filePath).delete();
		mCacheList.remove(key);
	}

	@SuppressLint("HandlerLeak")
	private void queryFromCacheFile(final Cacheable cacheable) {

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
				showStatus(cacheable.getCacheKey(), "try read cache data from file");
				String filePath = mCacheDir + "/ " + cacheable.getCacheKey();
				String cacheContent = FileUtil.read(filePath);
				JsonData cacheData = JsonData.create(cacheContent);

				Message msg = Message.obtain();
				msg.what = REQUEST_CACHE_SUCC;
				msg.obj = cacheData;
				handler.sendMessage(msg);
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	private void queryFromAssertCacheFile(final Cacheable cacheable) {

		// no in main thread, will no cause HandlerLeak
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REQUEST_ASSERT_CACHE_SUCC:
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
				showStatus(cacheable.getCacheKey(), "try read cache data from assert file");

				String cacheContent = FileUtil.readAssert(Cube.getInstance().getContext(), cacheable.getAssertInitDataPath());
				JsonData cacheData = JsonData.create(cacheContent);

				Message msg = Message.obtain();
				msg.what = REQUEST_ASSERT_CACHE_SUCC;
				msg.obj = cacheData;
				handler.sendMessage(msg);
			}
		}).start();
	}

	private void processCacheData(JsonData cacheData, Cacheable cacheable) {

		if (null != cacheData && cacheData.has("data")) {
			int lastTime = cacheData.optInt("time");
			JsonData data = cacheData.optJson("data");

			if (System.currentTimeMillis() / 1000 - lastTime > cacheable.getCacheTime()) {
				showStatus(cacheable.getCacheKey(), "onCachedPreviousData");
				cacheable.onCachedPreviousData(data);
			} else {
				showStatus(cacheable.getCacheKey(), "onCacheData");
				cacheable.onCacheData(data);
			}
		} else {

			showStatus(cacheable.getCacheKey(), "onNoCacheDataAvailable");
			cacheable.onNoCacheDataAvailable();
		}
	}

	private void setCacheData(String key, JsonData data) {
		showStatus(key, "set cache to runtime cache list");
		mCacheList.put(key, data);
	}

	private void showStatus(String cacheKey, String msg) {
		Log.d("cube_request", String.format("%s  %s", cacheKey, msg));
	}
}
