package com.srain.cube.image.imple;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;

import com.srain.cube.util.Version;

/**
 * 
 * Use a Thead pool to manager the thread.
 * 
 * @author huqiu.lhq
 * 
 */
public class DefaultExecutor implements Executor {

	private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
	private static final int KEEP_ALIVE_TIME = 1;

	private static int sNUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
	private static DefaultExecutor sInstance = null;

	private final ThreadPoolExecutor mThreadPool;
	private final BlockingQueue<Runnable> mTaskWorkQueue;

	static {

		KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
		sInstance = new DefaultExecutor();
	}

	public static DefaultExecutor getInstance() {
		return sInstance;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public static class LinkedBlockingStack<T> extends LinkedBlockingDeque<T> {

		private static final long serialVersionUID = -4114786347960826192L;

		@Override
		public boolean offer(T e) {
			return super.offerFirst(e);
		}

		@Override
		public T remove() {
			return super.removeFirst();
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("HandlerLeak")
	private DefaultExecutor() {

		if (Version.hasGingerbread()) {
			mTaskWorkQueue = new LinkedBlockingStack<Runnable>();
		} else {
			mTaskWorkQueue = new LinkedBlockingQueue<Runnable>();
		}
		sNUMBER_OF_CORES = 2;
		mThreadPool = new ThreadPoolExecutor(sNUMBER_OF_CORES, sNUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mTaskWorkQueue, new DefaultThreadFactory());
		if (Version.hasGingerbread()) {
			mThreadPool.allowCoreThreadTimeOut(true);
		}
	}

	/**
	 * The default thread factory
	 */
	static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private static final String sPre = "image-excutor-pool-";
		private static final String sPost = "-thread-";

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

	@Override
	public void execute(Runnable command) {
		mThreadPool.execute(command);
	}
}