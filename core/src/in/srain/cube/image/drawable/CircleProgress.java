package in.srain.cube.image.drawable;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import in.srain.cube.util.CLog;

/**
 * Created by bruce on 11/4/14.
 */
public class CircleProgress extends Drawable {
    private String prefixText = "";
    private String suffixText = "%";

    private Paint mTextPaint;
    private RectF rectF = new RectF();
    private float mTextSize;
    private int textColor = Color.WHITE;
    private int progress = 0;
    private int mMax = 100;
    private int mFinishedColor = Color.rgb(66, 145, 241);
    private int mUnfinishedColor = Color.rgb(204, 204, 204);
    private Paint mPaint = new Paint();

    private int mSize;

    public CircleProgress(int textSize) {
        this.mTextSize = textSize;

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);

        mPaint.setAntiAlias(true);
        setPainters();
    }

    public void setSize(int size) {
        mSize = size;
    }

    public void setPainters() {
        mTextPaint.setColor(textColor);
        mTextPaint.setTextSize(mTextSize);
    }

    public int getProgress() {
        return progress;
    }

    @SuppressWarnings("unuesd")
    public void setProgress(int progress) {
        this.progress = progress;
        if (this.progress > getMax()) {
            this.progress %= getMax();
        }
        setPainters();
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        if (max > 0 && this.mMax != max) {
            this.mMax = max;
            setPainters();
        }
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        if (textSize != this.mTextSize) {
            this.mTextSize = textSize;
            this.setPainters();
        }
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        if (this.textColor != textColor) {
            this.textColor = textColor;
            this.setPainters();
        }
    }

    public int getFinishedColor() {
        return mFinishedColor;
    }

    public void setFinishedColor(int finishedColor) {
        if (this.mFinishedColor != finishedColor) {
            this.mFinishedColor = finishedColor;
            this.setPainters();
        }
    }

    public int getUnfinishedColor() {
        return mUnfinishedColor;
    }

    public void setUnfinishedColor(int unfinishedColor) {
        this.mUnfinishedColor = unfinishedColor;
        this.setPainters();
    }

    public String getPrefixText() {
        return prefixText;
    }

    public void setPrefixText(String prefixText) {
        this.prefixText = prefixText;
        this.setPainters();
    }

    public String getSuffixText() {
        return suffixText;
    }

    public void setSuffixText(String suffixText) {
        this.suffixText = suffixText;
        this.setPainters();
    }

    public String getDrawText() {
        return getPrefixText() + getProgress() + getSuffixText();
    }

    public float getProgressPercentage() {
        return getProgress() / (float) getMax();
    }

    @Override
    public void draw(Canvas canvas) {
        CLog.d("test", "draw");
        float yHeight = getProgress() / (float) getMax() * mSize;
        float radius = mSize / 2f;
        float angle = (float) (Math.acos((radius - yHeight) / radius) * 180 / Math.PI);
        float startAngle = 90 + angle;
        float sweepAngle = 360 - angle * 2;
        mPaint.setColor(getUnfinishedColor());
        canvas.drawArc(rectF, startAngle, sweepAngle, false, mPaint);

        canvas.save();
        canvas.rotate(180, mSize / 2, mSize / 2);
        mPaint.setColor(getFinishedColor());
        canvas.drawArc(rectF, 270 - angle, angle * 2, false, mPaint);
        canvas.restore();

        String text = getDrawText();
        if (!TextUtils.isEmpty(text)) {
            float textHeight = mTextPaint.descent() + mTextPaint.ascent();
            canvas.drawText(text, (mSize - mTextPaint.measureText(text)) / 2.0f, (mSize - textHeight) / 2.0f, mTextPaint);
        }
    }

    @Override
    public void setAlpha(int i) {
        mTextPaint.setAlpha(i);
        mPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mTextPaint.setColorFilter(colorFilter);
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
