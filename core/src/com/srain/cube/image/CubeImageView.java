package com.srain.cube.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

/**
 * Sub-class of ImageView which:
 * <ul>
 * <li>
 * automatically notifies the drawable when it is being displayed.
 * </ul>
 * 
 * Most of the code is taken from the Android best practice of displaying Bitmaps
 * 
 * <a href="http://developer.android.com/training/displaying-bitmaps/index.html">Displaying Bitmaps Efficiently</a>.
 */
public class CubeImageView extends ImageView {

	private String mUrl = "";
	private int mSpecifiedWidth = 0;
	private int mSpecifiedHeight = 0;
	private ImageLoader mImageLoader;

	private ImageReuseInfo mImageReuseInfo;

	private ImageTask mImageTask;
	private Matrix mMatrix = new Matrix();
	private Matrix mDrawMatrix = null;

	private RectF mTempSrc = new RectF();
	private RectF mTempDst = new RectF();

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

		super.onDetachedFromWindow();
	}

	/**
	 * @see android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)
	 */
	@Override
	public void setImageDrawable(Drawable drawable) {
		updateDrawable(drawable);

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

	private void updateDrawable(Drawable drawable) {
		if (drawable == null || !(drawable instanceof CubeBitmapDrawable)) {
			return;
		}

		CubeBitmapDrawable d = (CubeBitmapDrawable) drawable;
		Bitmap bitmap = d.getBitmap();
		if (bitmap == null) {
			return;
		}

		int dwidth = bitmap.getScaledWidth(d.getTargetDensity());
		int dheight = bitmap.getScaledHeight(d.getTargetDensity());

		ScaleType scaleType = getScaleType();
		int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
		int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

		boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);

		if (ScaleType.FIT_XY == scaleType) {
			return;
		} else {

			if (ScaleType.MATRIX == scaleType) {
				// Use the specified matrix as-is.
				if (mMatrix.isIdentity()) {
					mDrawMatrix = null;
				} else {
					mDrawMatrix = mMatrix;
				}
			} else if (fits) {
				// The bitmap fits exactly, no transform needed.
				mDrawMatrix = null;
			} else if (ScaleType.CENTER == scaleType) {
				// Center bitmap in view, no scaling.
				mDrawMatrix = mMatrix;
				mDrawMatrix.setTranslate((int) ((vwidth - dwidth) * 0.5f + 0.5f), (int) ((vheight - dheight) * 0.5f + 0.5f));
			} else if (ScaleType.CENTER_CROP == scaleType) {
				mDrawMatrix = mMatrix;

				float scale;
				float dx = 0, dy = 0;

				if (dwidth * vheight > vwidth * dheight) {
					scale = (float) vheight / (float) dheight;
					dx = (vwidth - dwidth * scale) * 0.5f;
				} else {
					scale = (float) vwidth / (float) dwidth;
					dy = (vheight - dheight * scale) * 0.5f;
				}

				mDrawMatrix.setScale(scale, scale);
				mDrawMatrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
			} else if (ScaleType.CENTER_INSIDE == scaleType) {
				mDrawMatrix = mMatrix;
				float scale;
				float dx;
				float dy;

				if (dwidth <= vwidth && dheight <= vheight) {
					scale = 1.0f;
				} else {
					scale = Math.min((float) vwidth / (float) dwidth, (float) vheight / (float) dheight);
				}

				dx = (int) ((vwidth - dwidth * scale) * 0.5f + 0.5f);
				dy = (int) ((vheight - dheight * scale) * 0.5f + 0.5f);

				mDrawMatrix.setScale(scale, scale);
				mDrawMatrix.postTranslate(dx, dy);
			} else {
				// Generate the required transform.
				mTempSrc.set(0, 0, dwidth, dheight);
				mTempDst.set(0, 0, vwidth, vheight);

				mDrawMatrix = mMatrix;
				mDrawMatrix.setRectToRect(mTempSrc, mTempDst, scaleTypeToScaleToFit(scaleType));
			}
		}
	}

	private static Matrix.ScaleToFit scaleTypeToScaleToFit(ScaleType st) {
		// ScaleToFit enum to their corresponding Matrix.ScaleToFit values
		return sS2FArray[st.ordinal() - 1];
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
		int width = getWidth();
		int height = getHeight();

		boolean isFullyWrapContent = getLayoutParams() != null && getLayoutParams().height == LayoutParams.WRAP_CONTENT && getLayoutParams().width == LayoutParams.WRAP_CONTENT;
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
		mImageLoader.loadImage(this, mUrl, width, height, mImageReuseInfo);
	}

	public void setHoldingImageTask(ImageTask imageTask) {
		mImageTask = imageTask;
	}

	public ImageTask getHoldingImageTask() {
		return mImageTask;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mDrawMatrix != null) {
			canvas.concat(mDrawMatrix);
		}
		super.onDraw(canvas);
	}
}
