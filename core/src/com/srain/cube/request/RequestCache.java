package com.srain.cube.request;

import java.io.File;
import java.util.HashMap;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.srain.cube.concurrent.SimpleExcutor;
import com.srain.cube.file.FileUtil;

/**
 * 
 * @author http://www.liaohuqiu.net
 */
public class RequestCache {

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

	public interface ICacheable<T> {

		public int getCacheTime();

		public String getCacheKey();

		public String getAssertInitDataPath();

		/**
		 * We need to process the data from data source, do some filter of convert the structure.
		 * 
		 * As the "Assert Data" is a special data source, we also need to do the same work.
		 */
		public T processRawDataFromCache(JsonData jsonData);

		public void onNoCacheDataAvailable();

		public void onCacheData(T previousJsonData, boolean outofDate);
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
		showStatus(String.format("init, cache dir::%s", mCacheDir));
	}

	public static RequestCache getInstance() {
		if (null == mInstance)
			mInstance = new RequestCache();
		return mInstance;
	}

	public <T> void requestCache(ICacheable<T> cacheable) {
		ReadCacheTask<T> task = new ReadCacheTask<T>(cacheable);
		task.query();
	}

	public <T> void cacheRequest(final ICacheable<T> cacheable, final JsonData data) {
		showStatus(String.format("cacheRequest, key:%s", cacheable.getCacheKey()));
		new Thread(new Runnable() {
			@Override
			public void run() {
				String filePath = mCacheDir + "/ " + cacheable.getCacheKey();
				JsonData jsonData = makeCacheFormatJsonData(data, 0);
				setCacheData(cacheable.getCacheKey(), jsonData);
				FileUtil.write(filePath, jsonData.toString());
			}
		}, "SimpleRequestBase-Cache").start();
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

	private class ReadCacheTask<T1> implements Runnable {

		private ICacheable<T1> mCacheable;

		private JsonData mRawData;
		private T1 mResult;
		private int mWorkType = 0;

		public ReadCacheTask(ICacheable<T1> cacheable) {
			mCacheable = cacheable;
		}

		void query() {
			String cacheKey = mCacheable.getCacheKey();

			// try to find in runtime cache
			if (mCacheList.containsKey(cacheKey)) {
				showStatus(String.format("exsit in list, key:%s", mCacheable.getCacheKey()));
				mRawData = mCacheList.get(cacheKey);
				processRawCacheData();
				return;
			}

			// try read from cache data
			String filePath = mCacheDir + "/ " + mCacheable.getCacheKey();
			File file = new File(filePath);
			if (file.exists()) {
				queryFromCacheFile();
				return;
			}

			// try to read from assert cache file
			String assertInitDataPath = mCacheable.getAssertInitDataPath();
			if (assertInitDataPath != null && assertInitDataPath.length() > 0) {
				queryFromAssertCacheFile();
				return;
			}

			showStatus(String.format("cache file not exist, key:%s", mCacheable.getCacheKey()));
			mCacheable.onNoCacheDataAvailable();
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

		private void queryFromCacheFile() {
			mWorkType = DO_READ_FROM_FILE;
			SimpleExcutor.getInstance().execute(this);
		}

		private void doQueryFromCacheFileInBackground() {

			showStatus(String.format("try read cache data from file, key:%s", mCacheable.getCacheKey()));
			String filePath = mCacheDir + "/ " + mCacheable.getCacheKey();
			String cacheContent = FileUtil.read(filePath);
			mRawData = JsonData.create(cacheContent);

			Message msg = Message.obtain();
			msg.what = AFTER_READ_FROM_FILE;
			msg.obj = this;
			sHandler.sendMessage(msg);
		}

		private void queryFromAssertCacheFile() {
			mWorkType = DO_READ_FROM_ASSERT;
			SimpleExcutor.getInstance().execute(this);
		}

		private void queryFromAssertCacheFileInBackground() {
			showStatus(String.format("try read cache data from assert file: %s", mCacheable.getCacheKey()));

			String cacheContent = FileUtil.readAssert(mContext, mCacheable.getAssertInitDataPath());
			JsonData rawData = JsonData.create(cacheContent);
			mRawData = makeCacheFormatJsonData(rawData, -2);
			setCacheData(mCacheable.getCacheKey(), mRawData);

			Message msg = Message.obtain();
			msg.what = AFTER_READ_FROM_ASSERT;
			msg.obj = this;
			sHandler.sendMessage(msg);
		}

		void doConvertInBackground() {

			JsonData data = mRawData.optJson("data");
			mResult = mCacheable.processRawDataFromCache(data);

			Message msg = Message.obtain();
			msg.what = AFTER_CONVERT;
			msg.obj = this;
			sHandler.sendMessage(msg);
		}

		private void processRawCacheData() {
			if (null != mRawData && mRawData.has("data")) {
				mWorkType = DO_CONVERT;
				new Thread(this).start();
			} else {
				showStatus(String.format("onNoCacheDataAvailable, key:%s", mCacheable.getCacheKey()));
				mCacheable.onNoCacheDataAvailable();
			}
		}

		void done() {

			int lastTime = mRawData.optInt("time");
			long timeInterval = System.currentTimeMillis() / 1000 - lastTime;
			boolean outOfDate = timeInterval > mCacheable.getCacheTime() || timeInterval < 0;
			mCacheable.onCacheData(mResult, outOfDate);
		}
	}

	private void setCacheData(String key, JsonData data) {
		showStatus(String.format("set cache to runtime cache list, key:%s", key));
		mCacheList.put(key, data);
	}

	private void showStatus(String msg) {
	}
}