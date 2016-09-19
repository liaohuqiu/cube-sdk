package in.srain.cube.image.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import in.srain.cube.util.CubeDebug;

/**
 * A BitmapDrawable that keeps track of whether it is being displayed or cached.
 * <p/>
 * When the drawable is no longer being displayed or cached, {@link Bitmap#recycle() recycle()} will be called on this drawable's bitmap.
 * <p/>
 * Most of the code is taken from the Android best practice of displaying Bitmaps <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 */
public class RecyclingBitmapDrawable extends BitmapDrawable {

    private final static String LOG_TAG = CubeDebug.DEBUG_IMAGE_LOG_TAG;
    static final boolean DEBUG = false;

    private int mCacheRefCount = 0;
    private int mDisplayRefCount = 0;

    private boolean mHasBeenDisplayed;

    public RecyclingBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    /**
     * Notify the drawable that the displayed state has changed. Internally a count is kept so that the drawable knows when it is no longer being displayed.
     */
    public void setIsDisplayed(boolean isDisplayed) {
        synchronized (this) {
            if (isDisplayed) {
                mDisplayRefCount++;
                mHasBeenDisplayed = true;
            } else {
                mDisplayRefCount--;
            }
        }

        // Check to see if recycle() can be called
        checkState();
    }

    /**
     * Notify the drawable that the cache state has changed. Internally a count is kept so that the drawable knows when it is no longer being cached.
     */
    public void setIsCached(boolean isCached) {
        synchronized (this) {
            if (isCached) {
                mCacheRefCount++;
            } else {
                mCacheRefCount--;
            }
        }

        // Check to see if recycle() can be called
        checkState();
    }

    private synchronized void checkState() {
        // If the drawable cache and display ref counts = 0, and this drawable
        // has been displayed, then recycle
        if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed && hasValidBitmap()) {
            if (DEBUG) {
                Log.d(LOG_TAG, "No longer being used or cached so recycling. " + toString());
            }
            getBitmap().recycle();
        }
    }

    private synchronized boolean hasValidBitmap() {
        Bitmap bitmap = getBitmap();
        return bitmap != null && !bitmap.isRecycled();
    }

}
