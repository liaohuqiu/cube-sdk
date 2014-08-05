package in.srain.cube.util;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.util.Log;

/**
 * Class from encrypting string
 */
public class SystemWather {

	private static SystemWather sInstance = new SystemWather();

	private Context mContext;

	private SystemWather() {
	}

	public static SystemWather getInstance() {
		return sInstance;
	}

	public static void init(Context context) {
		sInstance.mContext = context;
	}

	private Timer mTimer;
	private TimerTask mTimerTask;

	public void run() {
		mTimerTask = new TimerTask() {

			@Override
			public void run() {

				MemoryInfo mi = new MemoryInfo();
				ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Activity.ACTIVITY_SERVICE);
				activityManager.getMemoryInfo(mi);
				Runtime runtime = Runtime.getRuntime();
				String s = String.format("free:%s%% %sKB total:%sKB max:%sKB ", runtime.freeMemory() * 100f / runtime.totalMemory(), runtime.freeMemory(), runtime.totalMemory() / 1024,
						runtime.maxMemory() / 1024);
				// s += String.format("native: free:%sKB total:%sKB max:%sKB", android.os.Debug.getNativeHeapFreeSize() / 1024, android.os.Debug.getNativeHeapAllocatedSize() / 1024,
				// android.os.Debug.getNativeHeapSize() / 1024);
				// s += String.format("| availMem:%sKB", mi.availMem / 1024);
				Log.d("memory", s);
			}
		};

		mTimer = new Timer();
		mTimer.schedule(mTimerTask, 1000, 1000);
	}
}
