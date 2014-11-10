package in.srain.cube.sample.ui.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import in.srain.cube.util.CLog;
import in.srain.cube.util.LocalDisplay;

import java.util.ArrayList;

public class StoreHouseHeader extends View {

    public ArrayList<StoreHouseBarItem> mItemList = new ArrayList<StoreHouseBarItem>();

    private int lineWidth = LocalDisplay.dp2px(1);
    private float scale = 1;
    private int mDropHeight = LocalDisplay.dp2px(40);
    private float internalAnimationFactor = 0.7f;
    private int horizontalRandomness = 850;

    private int[] startX = new int[]{0, 30, 52, 26, 60, 60, 85, 108, 108, 147, 177, 147, 176, 206, 228, 202};
    private int[] startY = new int[]{30, 0, 30, 30, 15, 30, 15, 0, 30, 0, 0, 32, 30, 0, 30, 30};
    private int[] endX = new int[]{22, 22, 30, 0, 60, 60, 60, 85, 85, 117, 147, 147, 198, 198, 206, 176};
    private int[] endY = new int[]{0, 0, 0, 30, 0, 15, 15, 15, 15, 0, 0, 0, 0, 0, 0, 30};

    private float mProgress = 0;

    private int mDrawWidth = 0;
    private int mDrawHeight = 0;
    private int mBoundHeight;
    private int mBoundsWidth;
    private int mOffsetX = 0;
    private int mOffsetY = 0;
    private float mBarDarkAlpha = 0.4f;

    private Transformation mTransformation = new Transformation();
    private boolean mIsInLoading = false;

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
            if (!mIsInLoading) {
                return;
            }
            if (!(animation instanceof StoreHouseBarItem)) {
                return;
            }
            StoreHouseBarItem item = (StoreHouseBarItem) animation;
            if (item.getIndex() == mItemList.size() - 1) {
                beginLoading();
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    public StoreHouseHeader(Context context) {
        super(context);
        init();
    }

    public StoreHouseHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setProgress(float progress) {
        mProgress = progress;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setBounds(getMeasuredWidth(), getMeasuredHeight());
    }

    public void setBounds(int width, int height) {
        mBoundsWidth = width;
        mBoundHeight = height;

        mOffsetX = (mBoundsWidth - mDrawWidth) / 2;
        mOffsetY = (mBoundHeight - mDrawHeight) / 2;
    }

    private void init() {

        for (int i = 0; i < startX.length; i++) {
            startX[i] = LocalDisplay.dp2px(startX[i] / 2);
            startY[i] = LocalDisplay.dp2px(startY[i] / 2);
            endX[i] = LocalDisplay.dp2px(endX[i] / 2);
            endY[i] = LocalDisplay.dp2px(endY[i] / 2);

            if (startX[i] > mDrawWidth) {
                mDrawWidth = startX[i];
            }
            if (endX[i] > mDrawWidth) {
                mDrawWidth = endX[i];
            }
            if (startY[i] > mDrawHeight) {
                mDrawHeight = startY[i];
            }
            if (endY[i] > mDrawHeight) {
                mDrawHeight = endY[i];
            }
        }

        for (int i = 0; i < startX.length; i++) {

            Point startPoint = new Point(startX[i], startY[i]);
            Point endPoint = new Point(endX[i], endY[i]);

            StoreHouseBarItem item = new StoreHouseBarItem(i, startPoint, endPoint, Color.WHITE, lineWidth);
            item.reset(horizontalRandomness);
            mItemList.add(item);
        }
    }

    public void beginLoading() {
        mIsInLoading = true;
        for (int i = 0; i < mItemList.size(); i++) {
            StoreHouseBarItem item = mItemList.get(i);
            item.setFillAfter(false);
            item.setFillEnabled(true);
            item.setFillBefore(false);
            item.setStartOffset(100 * i);
            item.setDuration(400);
            item.setAnimationListener(animationListener);
            item.start();
        }
        CLog.d("ptr-test", "beginLoading");
        invalidate();
    }

    public void loadFinish() {
        mIsInLoading = false;
        CLog.d("ptr-test", "loadFinish");
    }

    @Override
    public void onDraw(Canvas canvas) {
        float progress = mProgress;
        int c1 = canvas.save();
        int len = mItemList.size();

        for (int i = 0; i < mItemList.size(); i++) {

            canvas.save();
            StoreHouseBarItem storeHouseBarItem = mItemList.get(i);
            int offsetX = mOffsetX + storeHouseBarItem.midPoint.x;
            int offsetY = mOffsetY + storeHouseBarItem.midPoint.y;

            if (mIsInLoading) {
                storeHouseBarItem.getTransformation(getDrawingTime(), mTransformation);
                canvas.translate(offsetX, offsetY);
            } else {

                if (progress == 0) {
                    storeHouseBarItem.reset(horizontalRandomness);
                    continue;
                }

                float startPadding = (1 - internalAnimationFactor) * i / len;
                float endPadding = 1 - internalAnimationFactor - startPadding;

                // done
                if (progress == 1 || progress >= 1 - endPadding) {
                    canvas.translate(offsetX, offsetY);
                    storeHouseBarItem.setAlpha(mBarDarkAlpha);
                } else {
                    float realProgress;
                    if (progress <= startPadding) {
                        realProgress = 0;
                    } else {
                        realProgress = Math.min(1, (progress - startPadding) / internalAnimationFactor);
                    }
                    offsetX += storeHouseBarItem.translationX * (1 - realProgress);
                    offsetY += -mDropHeight * (1 - realProgress);
                    Matrix matrix = new Matrix();
                    matrix.postRotate((float) (360 * realProgress));
                    matrix.postScale(realProgress, realProgress);
                    matrix.postTranslate(offsetX, offsetY);
                    storeHouseBarItem.setAlpha(mBarDarkAlpha * realProgress);
                    canvas.concat(matrix);
                }
            }
            storeHouseBarItem.draw(canvas);
            canvas.restore();
        }
        if (mIsInLoading) {
            invalidate();
        }
        canvas.restoreToCount(c1);
    }
}