package com.srain.cube.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

/**
 * Sub-class of ImageView which:
 * <ul>
 * <li>
 * automatically notifies the Drawable when it is being displayed.
 * <li>
 * avoid requestLayout() when the placeholder image is replaced after image loaded.
 * <li>
 * adjustBounding is invalidate
 * </ul>
 * 
 * Most of the code is taken from the Android best practice of displaying Bitmaps
 * 
 * <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 */
public class CubeImageView extends ImageView {

	private String mLastUrl = "";
	private String mUrl = "";
	private int mSpecifiedWidth = 0;
	private int mSpecifiedHeight = 0;
	private ImageLoader mImageLoader;

	private ImageReuseInfo mImageReuseInfo;

	private ImageTask mImageTask;
	private Matrix mMatrix = new Matrix();

	private RectF mTempSrc = new RectF();
	private RectF mTempDst = new RectF();
	private Boolean mFitView = false;

	private static final Matrix.ScaleToFit[] sS2FArray = { Matrix.ScaleToFit.FILL, Matrix.ScaleToFit.START, Matrix.ScaleToFit.CENTER, Matrix.ScaleToFit.END };

	public CubeImageView(Context context) {
		super(context);
	}

	public CubeImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * @see android.widget.ImageView#onDetachedFromWindow()
	 */
	@Override
	protected void onDetachedFromWindow() {
		// This has been detached from Window, so clear the drawable
		setImageDrawable(null);

		if (null != mImageTask && null != mImageLoader) {
			mImageLoader.detachImageViewFromImageTask(mImageTask, this);
		}

		super.onDetachedFromWindow();
	}

	/**
	 * @see android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)
	 */
	@Override
	public void setImageDrawable(Drawable drawable) {

		// Keep hold of previous Drawable
		final Drawable previousDrawable = getDrawable();
		// Call super to set new Drawable
		super.setImageDrawable(drawable);

		// Notify new Drawable that it is being displayed
		notifyDrawable(drawable, true);

		// Notify old Drawable so it is no longer being displayed
		notifyDrawable(previousDrawable, false);
	}

	@Override
	public void setImageMatrix(Matrix matrix) {
		super.setImageMatrix(matrix);
		if (matrix != null && matrix.isIdentity()) {
			matrix = null;
		}

		// don't invalidate unless we're actually changing our matrix
		if (matrix == null && !mMatrix.isIdentity() || matrix != null && !mMatrix.equals(matrix)) {
			mMatrix.set(matrix);
		}
	}

	/**
	 * Notifies the drawable that it's displayed state has changed.
	 * 
	 * @param drawable
	 * @param isDisplayed
	 */
	private static void notifyDrawable(Drawable drawable, final boolean isDisplayed) {
		if (drawable instanceof RecyclingBitmapDrawable) {
			// The drawable is a CountingBitmapDrawable, so notify it
			((RecyclingBitmapDrawable) drawable).setIsDisplayed(isDisplayed);
		} else if (drawable instanceof LayerDrawable) {
			// The drawable is a LayerDrawable, so recurse on each layer
			LayerDrawable layerDrawable = (LayerDrawable) drawable;
			for (int i = 0, z = layerDrawable.getNumberOfLayers(); i < z; i++) {
				notifyDrawable(layerDrawable.getDrawable(i), isDisplayed);
			}
		}
	}

	public void loadImage(ImageLoader imageLoader, String url) {
		loadImage(imageLoader, url, 0, 0, null);
	}

	public void loadImage(ImageLoader imageLoader, String url, ImageReuseInfo imageReuseInfo) {
		loadImage(imageLoader, url, 0, 0, imageReuseInfo);
	}

	public void loadImage(ImageLoader imageLoader, String url, int specifiedSize) {
		loadImage(imageLoader, url, specifiedSize, specifiedSize, null);
	}

	public void loadImage(ImageLoader imageLoader, String url, int specifiedSize, ImageReuseInfo imageReuseInfo) {
		loadImage(imageLoader, url, specifiedSize, specifiedSize, imageReuseInfo);
	}

	public void loadImage(ImageLoader imageLoader, String url, int specifiedWidth, int specifieHeight) {
		loadImage(imageLoader, url, specifiedWidth, specifieHeight, null);
	}

	public void loadImage(ImageLoader imageLoader, String url, int specifiedWidth, int specifieHeight, ImageReuseInfo imageReuseInfo) {
		mImageLoader = imageLoader;
		mUrl = url;
		mSpecifiedWidth = specifiedWidth;
		mSpecifiedHeight = specifieHeight;
		mImageReuseInfo = imageReuseInfo;
		tryLoadImage(false);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		tryLoadImage(true);
	}

	private void tryLoadImage(boolean isOnLayout) {

		if (TextUtils.isEmpty(mUrl)) {
			return;
		}
		if (mLastUrl != null && mLastUrl.equals(mUrl)) {
			return;
		}

		int width = getWidth();
		int height = getHeight();

		ViewGroup.LayoutParams lyp = getLayoutParams();
		boolean isFullyWrapContent = lyp != null && lyp.height == LayoutParams.WRAP_CONTENT && lyp.width == LayoutParams.WRAP_CONTENT;
		// if the view's bounds aren't known yet, and this is not a wrap-content/wrap-content
		// view, hold off on loading the image.
		if (width == 0 && height == 0 && !isFullyWrapContent) {
			return;
		}

		if (mSpecifiedWidth != 0) {
			width = mSpecifiedWidth;
		}

		if (mSpecifiedHeight != 0) {
			height = mSpecifiedHeight;
		}

		// 1. Check the previous ImageTask related to this ImageView
		if (null != mImageTask) {

			// duplicated ImageTask, return directly.
			if (mImageTask.getRemoteUrl().equals(mUrl)) {
				return;
			}
			// ImageView is reused, detach it from the related ImageViews of the previous ImageTask.
			else {
				mImageLoader.detachImageViewFromImageTask(mImageTask, this);
			}
		}

		// 2. Let the ImageView hold this ImageTask. When ImageView is reused next time, check it in step 1.
		ImageTask imageTask = mImageLoader.createImageTask(mUrl, width, height, mImageReuseInfo);
		mImageTask = imageTask;

		// 3. Query cache, if hit, return at once.
		boolean hitCache = mImageLoader.queryCache(imageTask, this);
		if (hitCache) {
			return;
		} else {
			mImageLoader.addImageTask(mImageTask, this);
		}
	}

	/**
	 * Create Bitmap data according the ScaleType that set to this ImageView
	 * 
	 * @param src
	 * @return
	 */
	public Bitmap convertBitmap(Bitmap src) {
		Matrix matrix = new Matrix();
		// the original size of the Bitmap data.
		int dwidth = src.getWidth();
		int dheight = src.getHeight();

		// The ScaleType of the ImageView
		ScaleType scaleType = getScaleType();
		int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
		int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

		boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);

		int x = 0;
		int y = 0;
		int w = 0;
		int h = 0;
		float scale = 0;

		if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == scaleType) {
			return null;
		} else {
			mFitView = false;
			if (ScaleType.MATRIX == scaleType) {
				// Use the specified matrix as-is.
				if (mMatrix.isIdentity()) {
					matrix = null;
				} else {
					matrix = mMatrix;
				}
			} else if (fits) {
				// The bitmap fits exactly, no transform needed.
				matrix = null;
			} else if (ScaleType.CENTER == scaleType) {
				// Center bitmap in view, no scaling.
				matrix = mMatrix;
				matrix.setTranslate((int) ((vwidth - dwidth) * 0.5f + 0.5f), (int) ((vheight - dheight) * 0.5f + 0.5f));
			} else if (ScaleType.CENTER_CROP == scaleType) {
				matrix = mMatrix;

				// .vwidth
				// +-----+
				// |.....| vheight
				// +-----+
				//
				// case 1:
				// ..dwidth
				// +--------+
				// |........| dheight
				// +--------+
				//
				// case 2:
				// dwidth
				// +---+
				// |...| dheight
				// |...|
				// +---+

				// case 1, fit height, crop width:
				// if (dwidth / dheight > vwidth / vheight) {
				if (dwidth * vheight > vwidth * dheight) {
					scale = (float) dheight / (float) vheight;
					x = (int) ((dwidth - scale * vwidth) * 0.5f);
				}
				// case 2, fit width, crop height:
				else {
					scale = (float) dwidth / (float) vwidth;
					y = (int) ((dheight - vheight * scale) * 0.5f);
				}

				matrix.setScale(scale, scale);
				// matrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
			} else if (ScaleType.CENTER_INSIDE == scaleType) {
				matrix = mMatrix;
				float dx;
				float dy;

				if (dwidth <= vwidth && dheight <= vheight) {
					scale = 1.0f;
				} else {
					scale = Math.min((float) vwidth / (float) dwidth, (float) vheight / (float) dheight);
				}

				dx = (int) ((vwidth - dwidth * scale) * 0.5f + 0.5f);
				dy = (int) ((vheight - dheight * scale) * 0.5f + 0.5f);

				matrix.setScale(scale, scale);
				matrix.postTranslate(dx, dy);
			} else {
				// Generate the required transform.
				mTempSrc.set(0, 0, dwidth, dheight);
				mTempDst.set(0, 0, vwidth, vheight);

				matrix = mMatrix;
				matrix.setRectToRect(mTempSrc, mTempDst, sS2FArray[scaleType.ordinal() - 1]);
			}
		}
		if (null == matrix || matrix.isIdentity()) {
			return src;
		}

		Log.d("test", String.format("%s, %s %s => %s %s, %s %s", mImageTask, dwidth, dheight, vwidth, vheight, x, y));

		Bitmap bitmap = Bitmap.createBitmap(src, x, y, (int) (vwidth * scale), (int) (vheight * scale), matrix, true);
		Log.d("test", String.format("%s, %s %s", mImageTask, bitmap.getWidth(), bitmap.getHeight()));
		return bitmap;
	}

	private static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter) {

		if (x + width > source.getWidth()) {
			throw new IllegalArgumentException("x + width must be <= bitmap.width()");
		}
		if (y + height > source.getHeight()) {
			throw new IllegalArgumentException("y + height must be <= bitmap.height()");
		}

		// check if we can just return our argument unchanged
		if (!source.isMutable() && x == 0 && y == 0 && width == source.getWidth() && height == source.getHeight() && (m == null || m.isIdentity())) {
			return source;
		}

		int neww = width;
		int newh = height;
		Canvas canvas = new Canvas();
		Bitmap bitmap;
		Paint paint;

		Rect srcR = new Rect(x, y, x + width, y + height);
		RectF dstR = new RectF(0, 0, width, height);

		Config newConfig = Config.ARGB_8888;
		final Config config = source.getConfig();
		// GIF files generate null configs, assume ARGB_8888
		if (config != null) {
			switch (config) {
			case RGB_565:
				newConfig = Config.RGB_565;
				break;
			case ALPHA_8:
				newConfig = Config.ALPHA_8;
				break;
			// noinspection deprecation
			case ARGB_4444:
			case ARGB_8888:
			default:
				newConfig = Config.ARGB_8888;
				break;
			}
		}

		if (m == null || m.isIdentity()) {
			bitmap = Bitmap.createBitmap(neww, newh, newConfig);
			paint = null; // not needed
		} else {
			final boolean transformed = !m.rectStaysRect();

			RectF deviceR = new RectF();
			m.mapRect(deviceR, dstR);

			neww = Math.round(deviceR.width());
			newh = Math.round(deviceR.height());

			bitmap = Bitmap.createBitmap(neww, newh, transformed ? Config.ARGB_8888 : newConfig);

			canvas.translate(-deviceR.left, -deviceR.top);
			canvas.concat(m);

			paint = new Paint();
			paint.setFilterBitmap(filter);
			if (transformed) {
				paint.setAntiAlias(true);
			}
		}

		// The new bitmap was created from a known bitmap source so assume that
		// they use the same density
		bitmap.setDensity(source.getDensity());

		canvas.setBitmap(bitmap);
		canvas.drawBitmap(source, srcR, dstR, paint);
		canvas.setBitmap(null);

		return bitmap;
	}
}