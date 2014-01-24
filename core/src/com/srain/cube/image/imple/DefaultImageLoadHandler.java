package com.srain.cube.image.imple;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;

import com.srain.cube.image.CubeBitmapDrawable;
import com.srain.cube.image.CubeImageView;
import com.srain.cube.image.ImageTask;
import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.util.CLog;
import com.srain.cube.util.Version;

/**
 * A simple implementation of {@link ImageLoadHandler}.
 * 
 * This loader will put a background to ImageView when the image is loading.
 * 
 * @author huqiu.lhq
 */
public class DefaultImageLoadHandler implements ImageLoadHandler {

	private final static boolean DEBUG = CLog.DEBUG;
	private final static String Log_TAG = "cube_image";
	private final static String MSG_LOADING = "%s onLoading";
	private final static String MSG_LOAD_FINISH = "%s onLoadFinish %s";

	private Context mContext;
	private boolean mFadeInBitmap = false;
	private BitmapDrawable mLoadingBitmapDrawable;
	private boolean mResizeImageViewAfterLoad = false;

	public DefaultImageLoadHandler(Context context) {
		mContext = context;
	}

	/**
	 * If set to true, the image will fade-in once it has been loaded by the background thread.
	 */
	public void setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap = fadeIn;
	}

	public void setReszieImageViewAfterLoad(boolean resize) {
		mResizeImageViewAfterLoad = resize;
	}

	/**
	 * set the placeholder bitmap
	 */
	public void setLoadingBitmap(Bitmap loadingBitmap) {
		if (Version.hasHoneycomb()) {
			mLoadingBitmapDrawable = new CubeBitmapDrawable(mContext.getResources(), loadingBitmap);
		}
	}

	/**
	 * set the placeholder bitmap
	 */
	public void setLoadingBitmap(int loadingBitmap) {
		setLoadingBitmap(BitmapFactory.decodeResource(mContext.getResources(), loadingBitmap));
	}

	@Override
	public void onLoading(ImageTask imageTask, CubeImageView imageView) {
		if (DEBUG) {
			Log.d(Log_TAG, String.format(MSG_LOADING, imageTask));
		}
		if (Version.hasHoneycomb()) {
			if (mLoadingBitmapDrawable != null && imageView != null && imageView.getDrawable() != mLoadingBitmapDrawable) {
				imageView.setImageDrawable(mLoadingBitmapDrawable);
			}
		} else {
			imageView.setImageDrawable(null);
		}
	}

	@Override
	public void onLoadFinish(ImageTask imageTask, CubeImageView imageView, BitmapDrawable drawable) {
		if (DEBUG) {
			Log.d(Log_TAG, String.format(MSG_LOAD_FINISH, imageTask, drawable));
		}

		if (drawable != null) {

			if (mResizeImageViewAfterLoad) {
				int w = drawable.getIntrinsicWidth();
				int h = drawable.getIntrinsicHeight();
				if (w > 0 && h > 0) {
					imageView.getLayoutParams().width = w;
					imageView.getLayoutParams().height = h;
				}
			}
			if (mFadeInBitmap) {
				final TransitionDrawable td = new TransitionDrawable(new Drawable[] { new ColorDrawable(android.R.color.transparent), drawable });
				imageView.setImageDrawable(td);
				td.startTransition(200);
			} else {

				Drawable d = imageView.getDrawable();
				if (d != null) {
					int w = d.getIntrinsicWidth();
					int h = d.getIntrinsicHeight();
					Log.d(Log_TAG, String.format("onLoadFinish %s %s %s %s", w, h, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight()));
				}
				imageView.setImageDrawable(drawable);
			}
		}
	}
}