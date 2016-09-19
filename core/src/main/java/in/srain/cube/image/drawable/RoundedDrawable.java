package in.srain.cube.image.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/**
 * A drawable with rounded corners;
 * CenterCrop
 *
 * @author http://www.liaohuqiu.net
 */
public class RoundedDrawable extends Drawable {

    protected final float mCornerRadius;

    protected final RectF mRect = new RectF();
    protected final BitmapShader mBitmapShader;
    protected final Paint mPaint;
    private int mBitmapWidth;
    private int mBitmapHeight;

    public RoundedDrawable(Bitmap bitmap, float cornerRadius) {
        mCornerRadius = cornerRadius;

        mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setShader(mBitmapShader);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRect.set(0, 0, bounds.width(), bounds.height());

        Matrix shaderMatrix = new Matrix();
        shaderMatrix.setRectToRect(new RectF(0, 0, mBitmapWidth, mBitmapHeight), mRect, Matrix.ScaleToFit.FILL);
        mBitmapShader.setLocalMatrix(shaderMatrix);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
    }

    public static Bitmap transform(final Bitmap source, float margin, float radius) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius, radius, paint);

        if (source != output) {
            source.recycle();
        }

        return output;
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
