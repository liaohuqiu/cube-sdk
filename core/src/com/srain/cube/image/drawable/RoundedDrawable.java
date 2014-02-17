package com.srain.cube.image.drawable;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * A drawable with rounded corners;
 * 
 * @author srain
 */
public class RoundedDrawable extends Drawable {

	protected final float mCornerRadius;
	protected final int mMargin;

	protected final RectF mRect = new RectF();
	protected final BitmapShader mBitmapShader;
	protected final Paint mPaint;

	public RoundedDrawable(Bitmap bitmap, float cornerRadius, int margin) {
		mCornerRadius = cornerRadius;
		mMargin = margin;

		mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setShader(mBitmapShader);
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		mRect.set(mMargin, mMargin, bounds.width() - mMargin, bounds.height() - mMargin);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}
}
