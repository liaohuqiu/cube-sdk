package in.srain.cube.image.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.ViewGroup;
import in.srain.cube.image.CubeImageView;
import in.srain.cube.image.ImageTask;
import in.srain.cube.image.drawable.RoundedDrawable;
import in.srain.cube.image.iface.ImageLoadHandler;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;
import in.srain.cube.util.Version;

/**
 * A simple implementation of {@link ImageLoadHandler}.
 * <p/>
 * This loader will put a background to ImageView when the image is loading.
 *
 * @author http://www.liaohuqiu.net
 */
public class DefaultImageLoadHandler implements ImageLoadHandler {

    private final static boolean DEBUG = CubeDebug.DEBUG_IMAGE;
    private final static String LOG_TAG = CubeDebug.DEBUG_IMAGE_LOG_TAG;
    private final static String MSG_LOADING = "%s => %s handler on loading";
    private final static String MSG_LOAD_ERROR = "%s => %s handler on load error";
    private final static String MSG_LOAD_FINISH = "%s => %s handler on load finish: %s %s %s %s";

    private Context mContext;
    private final static int DISPLAY_FADE_IN = 0x01;
    private final static int DISPLAY_ROUNDED = 0x02;

    private int mDisplayTag = DISPLAY_FADE_IN;

    private Drawable mLoadingDrawable;
    private Drawable mErrorDrawable;

    private int mLoadingColor = -1;
    private float mCornerRadius = 10;

    private boolean mResizeImageViewAfterLoad = false;

    public DefaultImageLoadHandler(Context context) {
        mContext = context;
        mLoadingDrawable = new ColorDrawable(0xfff1f1f1);
        mErrorDrawable = new ColorDrawable(Color.RED);
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        if (fadeIn) {
            mDisplayTag |= DISPLAY_FADE_IN;
        } else {
            mDisplayTag &= ~DISPLAY_FADE_IN;
        }
    }

    /**
     *
     */
    public void setImageRounded(boolean rounded, float cornerRadius) {
        if (rounded) {
            mDisplayTag |= DISPLAY_ROUNDED;
            mCornerRadius = cornerRadius;
        } else {
            mDisplayTag &= ~DISPLAY_ROUNDED;
        }
    }

    public void setResizeImageViewAfterLoad(boolean resize) {
        mResizeImageViewAfterLoad = resize;
    }

    /**
     * set the placeholder bitmap
     */
    public void setLoadingBitmap(Bitmap loadingBitmap) {
        if (Version.hasHoneycomb()) {
            mLoadingDrawable = new BitmapDrawable(mContext.getResources(), loadingBitmap);
        }
    }

    /**
     * set the placeholder Image
     *
     * @param loadingBitmap the resources id
     */
    public void setLoadingResources(int loadingBitmap) {
        setLoadingBitmap(BitmapFactory.decodeResource(mContext.getResources(), loadingBitmap));
    }

    public void setErrorBitmap(Bitmap bitmap) {
        if (Version.hasHoneycomb()) {
            mErrorDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
        }
    }

    /**
     * set the error Image
     *
     * @param resId
     */
    public void setErrorResources(int resId) {
        setErrorBitmap(BitmapFactory.decodeResource(mContext.getResources(), resId));
    }

    /**
     * set the placeholder by color
     *
     * @param color
     */
    public void setLoadingImageColor(int color) {
        mLoadingColor = color;
        mLoadingDrawable = new ColorDrawable(color);
    }

    /**
     * set the placeholder image by color string like: #ffffff
     *
     * @param colorString
     */
    public void setLoadingImageColor(String colorString) {
        setLoadingImageColor(Color.parseColor(colorString));
    }

    @Override
    public void onLoading(ImageTask imageTask, CubeImageView imageView) {
        if (imageView == null) {
            return;
        }
        if (DEBUG) {
            CLog.d(LOG_TAG, MSG_LOADING, imageTask, imageView);
        }
        if (Version.hasHoneycomb()) {
            if (mLoadingDrawable != null && imageView != null && imageView.getDrawable() != mLoadingDrawable) {
                imageView.setImageDrawable(mLoadingDrawable);
            }
        } else {
            imageView.setImageDrawable(null);
        }
    }

    @Override
    public void onLoadError(ImageTask imageTask, CubeImageView imageView, int errorCode) {
        if (DEBUG) {
            CLog.d(LOG_TAG, MSG_LOAD_ERROR, imageTask, imageView);
        }
        if (imageView != null) {
            if (Version.hasHoneycomb()) {
                if (mErrorDrawable != null && imageView != null && imageView.getDrawable() != mErrorDrawable) {
                    imageView.setImageDrawable(mErrorDrawable);
                }
            } else {
                imageView.setImageDrawable(null);
            }
            imageView.setImageDrawable(mErrorDrawable);
        }
    }

    @Override
    public void onLoadFinish(ImageTask imageTask, CubeImageView imageView, BitmapDrawable drawable) {

        if (imageView == null) {
            return;
        }

        Drawable d = drawable;
        if (drawable != null) {

            if (mResizeImageViewAfterLoad) {
                int w = drawable.getBitmap().getWidth();
                int h = drawable.getBitmap().getHeight();

                if (w > 0 && h > 0) {

                    ViewGroup.LayoutParams lyp = imageView.getLayoutParams();
                    if (lyp != null) {
                        lyp.width = w;
                        lyp.height = h;
                        imageView.setLayoutParams(lyp);
                    }
                }
            }

            // RoundedDrawable will not recycle automatically when API level is lower than 11
            if ((mDisplayTag & DISPLAY_ROUNDED) == DISPLAY_ROUNDED && Version.hasHoneycomb()) {
                d = new RoundedDrawable(drawable.getBitmap(), mCornerRadius);
            }
            if ((mDisplayTag & DISPLAY_FADE_IN) == DISPLAY_FADE_IN) {
                int loadingColor = android.R.color.transparent;
                if (mLoadingColor != -1 && (mDisplayTag & DISPLAY_ROUNDED) != DISPLAY_ROUNDED) {
                    loadingColor = mLoadingColor;
                }
                final TransitionDrawable td = new TransitionDrawable(new Drawable[]{new ColorDrawable(loadingColor), d});
                imageView.setImageDrawable(td);
                td.startTransition(200);
            } else {

                if (DEBUG) {
                    Drawable oldDrawable = imageView.getDrawable();
                    int w = 0, h = 0;
                    if (oldDrawable != null) {
                        w = oldDrawable.getIntrinsicWidth();
                        h = oldDrawable.getIntrinsicHeight();
                    }
                    CLog.d(LOG_TAG, MSG_LOAD_FINISH,
                            imageTask, imageView, w, h, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                }
                imageView.setImageDrawable(drawable);
            }
        }
    }
}