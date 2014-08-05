package net.liaohuqiu.cube.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import net.liaohuqiu.cube.util.Version;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * 
 * Use a Thead pool to manager the thread.
 * 
 * @author http://www.liaohuqiu.net
 * 
 */
public class SimpleExcutor {

	private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
	private static final int KEEP_ALIVE_TIME = 1;

	private static SimpleExcutor sInstance = null;

	private final ThreadPoolExecutor mThreadPool;
	private final BlockingQueue<Runnable> mTaskWorkQueue;

	static {

		KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
		sInstance = new SimpleExcutor();
	}

	public static SimpleExcutor getInstance() {
		return sInstance;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private SimpleExcutor() {
		mTaskWorkQueue = new LinkedBlockingQueue<Runnable>();
		mThreadPool = new ThreadPoolExecutor(1, 1, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mTaskWorkQueue, new DefaultThreadFactory());
		if (Version.hasGingerbread()) {
			mThreadPool.allowCoreThreadTimeOut(true);
		}
	}

	/**
	 * The default thread factory
	 */
	static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private static final String sPre = "simple-excutor-pool-";
		private static final String sPost = "-thread-";
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		DefaultThreadFactory() {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = sPre + poolNumber.getAndIncrement() + sPost;
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
			return t;
		}
	}

	public void execute(Runnable runnable) {
		mThreadPool.execute(runnable);
	}
}