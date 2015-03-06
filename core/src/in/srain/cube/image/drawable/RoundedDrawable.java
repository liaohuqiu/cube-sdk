package in.srain.cube.image.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import in.srain.cube.util.CLog;

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

        CLog.d("test", "onBoundsChange: %s %s %s %s", bounds.width(), bounds.height(), mBitmapWidth, mBitmapHeight);
    }

    protected void onBoundsChange1(Rect bounds) {
        super.onBoundsChange(bounds);

        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        /**
         *    bitmap
         *  +--------+
         *  |        |
         *  +--------+
         *
         *   bounds
         *   +----+
         *   |    |
         *   +----+
         */
        if (mBitmapWidth * bounds.height() > mBitmapHeight * bounds.width()) {
            height = mBitmapHeight;
            width = height * bounds.width() / bounds.height();
        }
        /**
         *   bitmap
         *   +----+
         *   |    |
         *   |    |
         *   +----+
         *    bounds
         *  +--------+
         *  |        |
         *  +--------+
         */
        else {
            width = mBitmapWidth;
            height = width * bounds.height() / bounds.width();
        }
        x = (bounds.width() - width) / 2;
        y = (bounds.height() - height) / 2;
        mRect.set(x, y, width + x, height + y);
        CLog.d("test", "onBoundsChange %s %s / %s %s / %s %s %s %s", bounds.width(), bounds.height(), mBitmapWidth, mBitmapHeight,
                x, y, width, height
        );
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
