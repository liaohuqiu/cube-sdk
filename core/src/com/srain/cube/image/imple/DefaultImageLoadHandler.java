package com.srain.cube.image.imple;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.srain.cube.image.CubeImageView;
import com.srain.cube.image.ImageTask;
import com.srain.cube.image.iface.ImageLoadHandler;
import com.srain.cube.util.CLog;
import com.srain.cube.util.Version;

/**
 * A simple implementation of {@link ImageLoadHandler}.
 * 
 * This loader will put a backgound to imageview when the image is loading.
 * 
 * @author huqiu.lhq
 */
public class DefaultImageLoadHandler implements ImageLoadHandler {

	private final static boolean DEBUG = CLog.DEBUG;
	private final static String Log_TAG = "cube_image";
	private final static String MSG_LOADING = "%s onLoading";
	private final static String MSG_LOAD_FINISH = "%s onLoadFinish";

	private Context mContext;
	private boolean mFadeInBitmap = false;
	private BitmapDrawable mLoadingBitmapDrawable;

	public DefaultImageLoadHandler(Context context) {
		mContext = context;
	}

	/**
	 * If set to true, the image will fade-in once it has been loaded by the background thread.
	 */
	public void setImageFadeIn(boolean fadeIn) {
		mFadeInBitmap = fadeIn;
	}

	/**
	 * set the placeholer bitmap
	 */
	public void setLoadingBitmap(Bitmap loadingBitmap) {
		if (Version.hasHoneycomb()) {
			mLoadingBitmapDrawable = new BitmapDrawable(mContext.getResources(), loadingBitmap);
		}
	}

	/**
	 * set the placeholer bitmap
	 */
	public void setLoadingBitmap(int loadingBitmap) {
		setLoadingBitmap(BitmapFactory.decodeResource(mContext.getResources(), loadingBitmap));
	}

	@Override
	public void onLoading(ImageTask imageTask, WeakReference<CubeImageView> imageViewReference) {
		ImageView imageView = imageViewReference.get();
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
	public void onLoadFinish(ImageTask imageTask, WeakReference<CubeImageView> imageViewReference, BitmapDrawable drawable) {
		final ImageView imageView = imageViewReference.get();
		if (DEBUG) {
			Log.d(Log_TAG, String.format(MSG_LOAD_FINISH, imageTask));
		}

		if (imageView != null && drawable != null) {
			if (mFadeInBitmap) {
				final TransitionDrawable td = new TransitionDrawable(new Drawable[] { new ColorDrawable(android.R.color.transparent), drawable });
				imageView.setImageDrawable(td);
				td.startTransition(200);
			} else {
				imageView.setImageDrawable(drawable);
			}
		}
	}
}
