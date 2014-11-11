package in.srain.cube.sample.ui.views;


import android.graphics.*;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import in.srain.cube.util.CLog;
import in.srain.cube.util.LocalDisplay;

import java.util.Random;

/**
 * Created by srain on 11/6/14.
 */
public class StoreHouseBarItem extends Animation {

    private final Paint mPaint = new Paint();
    public PointF midPoint;
    public float translationX;
    private PointF mStartPoint;
    private PointF mEndPoint;
    private float mFromAlpha = 1.0f;
    private float mToAlpha = 0.4f;
    private PointF mCStartPoint;
    private PointF mCEndPoint;
    private int mLineWidth;
    private int mColor;
    private int mIndex;

    public StoreHouseBarItem(int index, PointF start, PointF end, int color, int lineWidth) {
        mIndex = index;
        mStartPoint = start;
        mEndPoint = end;

        midPoint = new PointF((start.x + end.x) / 2, (start.y + end.y) / 2);

        mCStartPoint = new PointF(mStartPoint.x - midPoint.x, mStartPoint.y - midPoint.y);
        mCEndPoint = new PointF(mEndPoint.x - midPoint.x, mEndPoint.y - midPoint.y);
        mColor = color;
        mLineWidth = lineWidth;

        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    public int getIndex() {
        return mIndex;
    }

    public void reset(int horizontalRandomness) {
        Random random = new Random();
        int randomNumber = -random.nextInt(horizontalRandomness * 2) + horizontalRandomness;
        translationX = randomNumber;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (mIndex == 15) {
            CLog.d("ptr-test", "applyTransformation: %s", interpolatedTime);
        }
        float alpha = mFromAlpha;
        alpha = alpha + ((mToAlpha - alpha) * interpolatedTime);
        setAlpha(alpha);
    }

    public void setAlpha(float alpha) {
        mPaint.setAlpha((int) (alpha * 255));
    }

    public void draw(Canvas canvas) {
        canvas.drawLine(mCStartPoint.x, mCStartPoint.y, mCEndPoint.x, mCEndPoint.y, mPaint);
    }
}