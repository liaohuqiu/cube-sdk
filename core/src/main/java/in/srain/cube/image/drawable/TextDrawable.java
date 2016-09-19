package in.srain.cube.image.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;

public class TextDrawable extends Drawable {

    private final String mText;
    private final Paint mPaint;
    private int mBackgroundColor = 0xfff1f1f1;
    private int mTextColor = Color.BLACK;
    private float mX = 0;
    private float mY = 0;
    private float mTextSize = 40;

    public TextDrawable(String text) {
        this.mText = text;

        mPaint = new Paint();
        mPaint.setTextSize(mTextSize);
        mPaint.setAntiAlias(true);

        setUp();
    }

    public void setTextSize(float size) {
        if (mTextSize != size) {
            mTextSize = size;
            setUp();
        }
    }

    private void setUp() {
        float width = mPaint.measureText(mText);
        float height = mPaint.descent() + mPaint.ascent();
        mX = (getBounds().width() - width) / 2;
        mY = (getBounds().height() - height) / 2;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        setUp();
    }

    @Override
    public void draw(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);
        canvas.drawRect(getBounds(), mPaint);

        mPaint.setColor(mTextColor);
        canvas.drawText(mText, mX, mY, mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
