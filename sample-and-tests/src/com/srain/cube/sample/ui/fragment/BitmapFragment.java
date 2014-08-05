package net.liaohuqiu.cube.sample.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import net.liaohuqiu.cube.sample.R;
import net.liaohuqiu.cube.sample.activity.TitleBaseFragment;

public class BitmapFragment extends TitleBaseFragment {

	private RectF mTempSrc = new RectF();
	private RectF mTempDst = new RectF();
	private static final Matrix.ScaleToFit[] sS2FArray = { Matrix.ScaleToFit.FILL, Matrix.ScaleToFit.START, Matrix.ScaleToFit.CENTER, Matrix.ScaleToFit.END };

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHeaderTitle("Bitmap");

		int vwidth = 720;
		int vheight = 720;

		View view = inflater.inflate(R.layout.fragment_bitmap, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.iv_bitmap);
		imageView.setBackgroundColor(Color.parseColor("#b8ebf7"));
		imageView.setLayoutParams(new LinearLayout.LayoutParams(vwidth, vheight));

		ScaleType scaleType = ScaleType.FIT_CENTER;
		Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.pic2);
		Matrix matrix = configureBounds(src, vwidth, vheight, ScaleType.FIT_END, new Matrix());

		RectF srcRectF = new RectF(0, 0, 480, 360);
		RectF rect = new RectF();
		matrix.mapRect(rect, srcRectF);
		Log.d("test", String.format("mapRect: %s => %s", srcRectF, rect));

		Rect srcRect = new Rect(0, 0, 480, 360);
		Bitmap bitmap = convert(src, srcRect, 480, 480, scaleType, matrix);
		Log.d("test", String.format("%s %s => %s %s", src.getWidth(), src.getHeight(), bitmap.getWidth(), bitmap.getHeight()));

		imageView.setImageBitmap(bitmap);

		return view;
	}

	private Bitmap convert1(Bitmap src, Rect srcR, int width, int height, ScaleType scaleType, Matrix m) {
		return null;
	}

	private Matrix configureBounds(Bitmap src, int vwidth, int vheight, ScaleType mScaleType, Matrix mMatrix) {

		Matrix mDrawMatrix = new Matrix();
		int dwidth = src.getWidth();
		int dheight = src.getHeight();

		boolean fits = (dwidth < 0 || vwidth == dwidth) && (dheight < 0 || vheight == dheight);

		if (dwidth <= 0 || dheight <= 0 || ScaleType.FIT_XY == mScaleType) {
			mDrawMatrix = null;
		} else {

			if (ScaleType.MATRIX == mScaleType) {
				// Use the specified matrix as-is.
				if (mMatrix.isIdentity()) {
					mDrawMatrix = null;
				} else {
					mDrawMatrix = mMatrix;
				}
			} else if (fits) {
				// The bitmap fits exactly, no transform needed.
				mDrawMatrix = null;
			} else if (ScaleType.CENTER == mScaleType) {
				// Center bitmap in view, no scaling.
				mDrawMatrix = mMatrix;
				mDrawMatrix.setTranslate((int) ((vwidth - dwidth) * 0.5f + 0.5f), (int) ((vheight - dheight) * 0.5f + 0.5f));
			} else if (ScaleType.CENTER_CROP == mScaleType) {
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
			} else if (ScaleType.CENTER_INSIDE == mScaleType) {
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
				mDrawMatrix.setRectToRect(mTempSrc, mTempDst, sS2FArray[mScaleType.ordinal() - 1]);
			}
		}

		return mDrawMatrix;
	}

	private Bitmap convert(Bitmap src, Rect srcR, int width, int height, ScaleType scaleType, Matrix m) {

		int neww = width;
		int newh = height;

		Canvas canvas = new Canvas();
		Bitmap bitmap;
		Paint paint;

		RectF dstR = new RectF(0, 0, neww, newh);

		Config newConfig = Config.ARGB_8888;
		final Config config = src.getConfig();
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
			RectF srcRectF = new RectF(srcR.left, srcR.top, srcR.width(), srcR.height());
			// RectF src1 = new RectF(0, 0,);
			m.mapRect(deviceR, dstR);
			dstR = deviceR;

			bitmap = Bitmap.createBitmap(neww, newh, transformed ? Config.ARGB_8888 : newConfig);
			Log.d("test", String.format("map: %s, %s => %s", srcR, srcRectF, deviceR));

			canvas.concat(m);

			paint = new Paint();
			// paint.setFilterBitmap(filter);
			if (transformed) {
				paint.setAntiAlias(true);
			}
		}

		// The new bitmap was created from a known bitmap source so assume that
		// they use the same density
		bitmap.setDensity(src.getDensity());

		canvas.setBitmap(bitmap);
		// srcR = new Rect(0, 0, 480, 360);
		// canvas.translate(0, -120);
		// dstR = new RectF(0, 0, 480, 480);
		// dstR = new RectF(0, 0, width, height);
		canvas.drawBitmap(src, srcR, dstR, paint);
		Log.d("test", String.format("drawBitmap %s => %s, %sx%s", srcR, dstR, canvas.getWidth(), canvas.getHeight()));
		canvas.setBitmap(null);

		return bitmap;
	}
}
