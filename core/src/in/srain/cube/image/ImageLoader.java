package in.srain.cube.image;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.image.iface.ImageLoadHandler;
import in.srain.cube.image.iface.ImageResizer;
import in.srain.cube.image.iface.ImageTaskExecutor;
import in.srain.cube.image.impl.DefaultImageLoadHandler;
import in.srain.cube.image.impl.DefaultImageResizer;
import in.srain.cube.image.impl.DefaultImageTaskExecutor;
import in.srain.cube.util.CLog;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Manager the ImageTask loading list,
 *
 * @author http://www.liaohuqiu.net
 */
public class ImageLoader {

    private static final String MSG_ATTACK_TO_RUNNING_TASK = "%s attach to running: %s";

    private static final String MSG_TASK_DO_IN_BACKGROUND = "%s doInBackground";
    private static final String MSG_TASK_FINISH = "%s onFinish";
    private static final String MSG_TASK_CANCEL = "%s onCancel";
    private static final String MSG_TASK_HIT_CACHE = "%s hit cache %s %s";

    protected static final boolean DEBUG = CLog.DEBUG_IMAGE;
    protected static final String Log_TAG = "cube_image";

    protected ImageTaskExecutor mImageTaskExecutor;
    protected ImageResizer mImageResizer;
    protected ImageProvider mImageProvider;
    protected ImageLoadHandler mImageLoadHandler;

    protected boolean mPauseWork = false;
    protected boolean mExitTasksEarly = false;

    private final Object mPauseWorkLock = new Object();
    private HashMap<String, LoadImageTask> mLoadWorkList;
    protected Context mContext;

    protected Resources mResources;

    public enum ImageTaskOrder {
        FIRST_IN_FIRST_OUT, LAST_IN_FIRST_OUT
    }

    public ImageLoader(Context context, ImageProvider imageProvider, ImageTaskExecutor imageTaskExecutor, ImageResizer imageResizer, ImageLoadHandler imageLoadHandler) {
        mContext = context;
        mResources = context.getResources();

        mImageProvider = imageProvider;
        mImageTaskExecutor = imageTaskExecutor;
        mImageResizer = imageResizer;
        mImageLoadHandler = imageLoadHandler;

        mLoadWorkList = new HashMap<String, LoadImageTask>();
    }

    public static ImageLoader createDefault(Context context) {
        DefaultImageLoadHandler imageLoadHandler = new DefaultImageLoadHandler(context);
        return new ImageLoader(context, ImageProvider.getDefault(context), DefaultImageTaskExecutor.getInstance(), DefaultImageResizer.getInstance(), imageLoadHandler);
    }

    public void setImageLoadHandler(ImageLoadHandler imageLoadHandler) {
        mImageLoadHandler = imageLoadHandler;
    }

    public ImageLoadHandler getImageLoadHandler() {
        return mImageLoadHandler;
    }

    public void setImageResizer(ImageResizer resizer) {
        mImageResizer = resizer;
    }

    public ImageResizer getImageResizer() {
        return mImageResizer;
    }

    public ImageProvider getImageProvider() {
        return mImageProvider;
    }

    /**
     * Load the image in advance.
     */
    public void preLoadImages(String[] urls) {
        int len = urls.length;
        len = 10;
        for (int i = 0; i < len; i++) {
            final ImageTask imageTask = new ImageTask(urls[i], 0, 0, null);
            addImageTask(imageTask, null);
        }
    }

    /**
     * Create an ImageTask.
     * <p/>
     * You can override this method to return a customized ImagetTask.
     *
     * @param url
     * @param requestWidth
     * @param requestHeight
     * @param imageReuseInfo
     * @return
     */
    public ImageTask createImageTask(String url, int requestWidth, int requestHeight, ImageReuseInfo imageReuseInfo) {
        return new ImageTask(url, requestWidth, requestWidth, imageReuseInfo);
    }

    /**
     * Detach the ImageView from the ImageTask.
     *
     * @param imageTask
     * @param imageView
     */
    public void detachImageViewFromImageTask(ImageTask imageTask, CubeImageView imageView) {
        imageTask.removeImageView(imageView);
        if (imageTask.isLoading()) {
            if (!imageTask.isPreLoad() && !imageTask.stillHasRelatedImageView()) {
                LoadImageTask task = mLoadWorkList.get(imageTask.getIdentityKey());
                if (task != null) {
                    task.cancel(true);
                }
                if (DEBUG) {
                    Log.d(Log_TAG, String.format("%s previous work is cancelled.", imageTask));
                }
            }
        }
    }

    /**
     * Add the ImageTask into loading list.
     *
     * @param imageTask
     * @param imageView
     */
    public void addImageTask(ImageTask imageTask, CubeImageView imageView) {
        LoadImageTask runningTask = mLoadWorkList.get(imageTask.getIdentityKey());
        if (runningTask != null) {
            if (imageView != null) {
                if (DEBUG) {
                    Log.d(Log_TAG, String.format(MSG_ATTACK_TO_RUNNING_TASK, imageTask, runningTask.getImageTask()));
                }
                runningTask.getImageTask().addImageView(imageView);
            }
            return;
        } else {
            imageTask.addImageView(imageView);
        }

        imageTask.onLoading(mImageLoadHandler);

        LoadImageTask loadImageTask = new LoadImageTask(imageTask);
        mLoadWorkList.put(imageTask.getIdentityKey(), loadImageTask);
        mImageTaskExecutor.execute(loadImageTask);
    }

    /**
     * Check weather this imageTask has cache Drawable data.
     */
    public boolean queryCache(ImageTask imageTask, CubeImageView imageView) {
        if (null == mImageProvider) {
            return false;
        }
        BitmapDrawable drawable = mImageProvider.getBitmapFromMemCache(imageTask);
        if (drawable == null) {
            return false;
        }

        if (DEBUG) {
            Log.d(Log_TAG, String.format(MSG_TASK_HIT_CACHE, imageTask, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
            if (drawable.getIntrinsicWidth() == 270) {
                Log.d(Log_TAG, String.format(MSG_TASK_HIT_CACHE, imageTask, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
            }
        }
        imageTask.addImageView(imageView);
        imageTask.onLoadFinish(drawable, mImageLoadHandler);
        return true;
    }

    public void setTaskOrder(ImageTaskOrder order) {
        if (null != mImageTaskExecutor) {
            mImageTaskExecutor.setTaskOrder(order);
        }
    }

    /**
     * Inner class to process the image loading task in background threads.
     *
     * @author http://www.liaohuqiu.net
     */
    private class LoadImageTask extends SimpleTask {

        private ImageTask mImageTask;
        private BitmapDrawable mDrawable;

        public LoadImageTask(ImageTask imageTask) {
            this.mImageTask = imageTask;
        }

        public ImageTask getImageTask() {
            return mImageTask;
        }

        @Override
        public void doInBackground() {
            if (DEBUG) {
                Log.d(Log_TAG, String.format(MSG_TASK_DO_IN_BACKGROUND, mImageTask));
            }
            Bitmap bitmap = null;
            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        if (DEBUG) {
                            Log.d(Log_TAG, String.format("%s wait to begin", mImageTask));
                        }
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }

            // If this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (!isCancelled() && !mExitTasksEarly && (mImageTask.isPreLoad() || mImageTask.stillHasRelatedImageView())) {
                try {
                    bitmap = mImageProvider.fetchBitmapData(mImageTask, mImageResizer);
                    mDrawable = mImageProvider.createBitmapDrawable(mResources, bitmap);
                    mImageProvider.addBitmapToMemCache(mImageTask.getIdentityKey(), mDrawable);
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onFinish() {
            if (DEBUG) {
                Log.d(Log_TAG, String.format(MSG_TASK_FINISH, mImageTask));
            }
            if (mExitTasksEarly) {
                return;
            }
            mLoadWorkList.remove(mImageTask.getIdentityKey());

            if (!isCancelled() && !mExitTasksEarly) {
                mImageTask.onLoadFinish(mDrawable, mImageLoadHandler);
            }
        }

        @Override
        public void onCancel() {
            if (DEBUG) {
                Log.d(Log_TAG, String.format(MSG_TASK_CANCEL, mImageTask));
            }
            mLoadWorkList.remove(mImageTask.getIdentityKey());
            mImageTask.onCancel();
        }
    }

    private void setPause(boolean pause) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pause;
            if (!pause) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    /**
     * Temporarily hand up work, you can call this when the view is scrolling.
     */
    public void pauseWork() {
        mExitTasksEarly = false;
        setPause(true);
        if (DEBUG) {
            Log.d(Log_TAG, String.format("work_status: pauseWork %s", this));
        }
    }

    /**
     * Resume the work
     */
    public void resumeWork() {
        mExitTasksEarly = false;
        setPause(false);
        if (DEBUG) {
            Log.d(Log_TAG, String.format("work_status: resumeWork %s", this));
        }
    }

    /**
     * Recover the from the work list
     */
    public void recoverWork() {
        if (DEBUG) {
            Log.d(Log_TAG, String.format("work_status: recoverWork %s", this));
        }
        mExitTasksEarly = false;
        setPause(false);
        Iterator<Entry<String, LoadImageTask>> it = (Iterator<Entry<String, LoadImageTask>>) mLoadWorkList.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, LoadImageTask> item = it.next();
            LoadImageTask task = item.getValue();
            task.restart();
            mImageTaskExecutor.execute(task);
        }
    }

    /**
     * Drop all the work, and leave it in the work list.
     */
    public void stopWork() {
        if (DEBUG) {
            Log.d(Log_TAG, String.format("work_status: stopWork %s", this));
        }
        mExitTasksEarly = true;
        setPause(false);

        if (null != mImageProvider) {
            mImageProvider.flushFileCache();
        }
    }

    /**
     * Drop all the work, clear the work list.
     */
    public void destroy() {
        mExitTasksEarly = true;
        setPause(false);

        Iterator<Entry<String, LoadImageTask>> it = (Iterator<Entry<String, LoadImageTask>>) mLoadWorkList.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, LoadImageTask> item = it.next();
            final LoadImageTask task = item.getValue();
            it.remove();
            if (task != null) {
                task.cancel(true);
            }
        }
        mLoadWorkList.clear();
    }
}
