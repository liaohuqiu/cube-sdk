package in.srain.cube.concurrent;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class which encapsulate a task that can execute in background thread and can be cancelled.
 *
 * @author http://www.liaohuqiu.net
 */
public abstract class SimpleTask implements Runnable {

    private static final int STATE_NEW = 0x01;
    private static final int STATE_RUNNING = 0x02;
    private static final int STATE_COMPLETING = 0x04;
    private static final int STATE_CANCELLED = 0x08;

    private static final int MSG_TASK_DONE = 0x01;
    private static InternalHandler sHandler = new InternalHandler();
    private Thread mCurrentThread;
    private AtomicInteger mState = new AtomicInteger(STATE_NEW);

    /**
     * A worker will execute this method in a background thread
     */
    public abstract void doInBackground();

    /**
     * will be called after doInBackground();
     */
    public abstract void onFinish();

    /**
     * When the Task is Cancelled.
     */
    protected void onCancel() {
    }

    /**
     * Restart the task, just set the state to {@link #STATE_NEW}
     */
    public void restart() {
        mState.set(STATE_NEW);
    }

    @Override
    public void run() {
        if (!mState.compareAndSet(STATE_NEW, STATE_RUNNING)) {
            return;
        }
        mCurrentThread = Thread.currentThread();
        doInBackground();
        sHandler.obtainMessage(MSG_TASK_DONE, this).sendToTarget();
    }

    /**
     * check whether this work is canceled.
     */
    public boolean isCancelled() {
        return mState.get() == STATE_CANCELLED;
    }

    /**
     * check whether this work has done
     *
     * @return
     */
    public boolean isDone() {
        return mState.get() == STATE_COMPLETING;
    }

    public void cancel(boolean mayInterruptIfRunning) {
        if (mState.get() >= STATE_COMPLETING) {
            return;
        } else {
            if (mState.get() == STATE_RUNNING && mayInterruptIfRunning && null != mCurrentThread) {
                try {
                    mCurrentThread.interrupt();
                } catch (Exception e) {
                }
            }
            mState.set(STATE_CANCELLED);
            onCancel();
        }
    }

    private static class InternalHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SimpleTask work = (SimpleTask) msg.obj;
            switch (msg.what) {
                case MSG_TASK_DONE:
                    work.mState.set(STATE_COMPLETING);
                    work.onFinish();
                    break;
                default:
                    break;
            }
        }
    }

    public static void postDelay(Runnable r, long delayMillis) {
        sHandler.postDelayed(r, delayMillis);
    }
}