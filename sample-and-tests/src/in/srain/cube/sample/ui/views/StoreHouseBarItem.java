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

    private Point mStartPoint;
    private Point mEndPoint;

    private float mFromAlpha = 1.0f;
    private float mToAlpha = 0.4f;

    private Point mCStartPoint;
    private Point mCEndPoint;

    public Point midPoint;
    private int mLineWidth;
    private int mColor;
    public float translationX;
    private final Paint mPaint = new Paint();

    public void reset(int horizontalRandomness) {
        Random random = new Random();
        int randomNumber = -random.nextInt(horizontalRandomness * 2) + horizontalRandomness;
        translationX = randomNumber;
    }

    public StoreHouseBarItem(Point start, Point end, int color, int lineWidth) {
        mStartPoint = start;
        mEndPoint = end;

        midPoint = new Point((start.x + end.x) / 2, (start.y + end.y) / 2);

        mCStartPoint = new Point(mStartPoint.x - midPoint.x, mStartPoint.y - midPoint.y);
        mCEndPoint = new Point(mEndPoint.x - midPoint.x, mEndPoint.y - midPoint.y);
        mColor = color;
        mLineWidth = lineWidth;

        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float alpha = mFromAlpha;
        t.setAlpha(alpha + ((mToAlpha - alpha) * interpolatedTime));
        mCurrentAlpha = alpha + ((mToAlpha - alpha) * interpolatedTime);
        CLog.d("ptr-test", "%,3f", mCurrentAlpha);
        mPaint.setAlpha((int) (mCurrentAlpha * 255));
    }

    public void setAlpha(float alpha) {
        mPaint.setAlpha((int) (alpha * 255));
    }

    private float mCurrentAlpha;

    public void draw(Canvas canvas) {
        canvas.drawLine(mCStartPoint.x, mCStartPoint.y, mCEndPoint.x, mCEndPoint.y, mPaint);
    }
}