package in.srain.cube.concurrent;

import android.annotation.TargetApi;
import android.os.Build;
import in.srain.cube.util.Version;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Use a Thread pool to manager the thread.
 *
 * @author http://www.liaohuqiu.net
 */
public class SimpleExecutor {

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final String sDefaultThreadNamePrefix = "simple-executor-pool-";

    private static SimpleExecutor sInstance = null;

    private final ThreadPoolExecutor mThreadPool;
    private final BlockingQueue<Runnable> mTaskWorkQueue;

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new SimpleExecutor(sDefaultThreadNamePrefix, 2, 4);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private SimpleExecutor(String threadNamePrefix, int corePoolSize, int maxPoolSize) {
        mTaskWorkQueue = new LinkedBlockingQueue<Runnable>();
        mThreadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mTaskWorkQueue, new DefaultThreadFactory(threadNamePrefix));
        if (Version.hasGingerbread()) {
            mThreadPool.allowCoreThreadTimeOut(true);
        }
    }

    public static SimpleExecutor getInstance() {
        return sInstance;
    }

    public static SimpleExecutor create(String threadNamePrefix, int corePoolSize, int maxPoolSize) {
        return new SimpleExecutor(threadNamePrefix, corePoolSize, maxPoolSize);
    }

    public void execute(Runnable runnable) {
        mThreadPool.execute(runnable);
    }

    /**
     * The default thread factory
     */
    static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private static final String sPost = "-thread-";
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private DefaultThreadFactory(String threadNamePrefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = threadNamePrefix + poolNumber.getAndIncrement() + sPost;
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
}