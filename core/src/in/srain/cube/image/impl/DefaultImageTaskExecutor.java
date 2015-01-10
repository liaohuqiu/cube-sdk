package in.srain.cube.image.impl;

import android.annotation.TargetApi;
import android.os.Build;
import in.srain.cube.concurrent.LinkedBlockingDeque;
import in.srain.cube.image.ImageLoader;
import in.srain.cube.image.iface.ImageTaskExecutor;
import in.srain.cube.util.Version;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Use a Thread pool to manager the thread.
 *
 * @author http://www.liaohuqiu.net
 */
public class DefaultImageTaskExecutor implements ImageTaskExecutor {

    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
    private static final int KEEP_ALIVE_TIME = 1;

    private static int sNUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private static DefaultImageTaskExecutor sInstance = null;

    private final ThreadPoolExecutor mThreadPool;
    private final LinkedBlockingStack<Runnable> mTaskWorkQueue;

    static {
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
        sInstance = new DefaultImageTaskExecutor();
    }

    public static DefaultImageTaskExecutor getInstance() {
        return sInstance;
    }

    public static class LinkedBlockingStack<T> extends LinkedBlockingDeque<T> {

        private static final long serialVersionUID = -4114786347960826192L;
        private int mImageTaskOrder = ImageLoader.TASK_ORDER_FIRST_IN_FIRST_OUT;

        public void setTaskOrder(int order) {
            mImageTaskOrder = order;
        }

        @Override
        public boolean offer(T e) {
            if (mImageTaskOrder == ImageLoader.TASK_ORDER_FIRST_IN_FIRST_OUT) {
                return super.offerFirst(e);
            } else {
                return super.offer(e);
            }
        }

        @Override
        public T remove() {
            if (mImageTaskOrder == ImageLoader.TASK_ORDER_LAST_IN_FIRST_OUT) {
                return super.removeFirst();
            } else {
                return super.remove();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private DefaultImageTaskExecutor() {

        mTaskWorkQueue = new LinkedBlockingStack<Runnable>();
        mThreadPool = new ThreadPoolExecutor(sNUMBER_OF_CORES, sNUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mTaskWorkQueue, new DefaultThreadFactory());
        if (Version.hasGingerbread()) {
            mThreadPool.allowCoreThreadTimeOut(true);
        } else {
            // Does nothing
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
        private static final String sPre = "image-executor-pool-";
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

    @Override
    public void setTaskOrder(int order) {
        mTaskWorkQueue.setTaskOrder(order);
    }
}