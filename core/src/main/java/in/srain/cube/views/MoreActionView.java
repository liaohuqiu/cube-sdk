package in.srain.cube.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.View;
import in.srain.cube.R;

public class MoreActionView extends View {
    private int mColor = 0xFFFFFFFF;
    private Paint mPaint;

    private float mDotRadius = 3;
    private float mDotSpan = 5;

    public MoreActionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.MoreActionView, 0, 0);
        if (arr != null) {
            if (arr.hasValue(R.styleable.MoreActionView_more_action_dot_radius)) {
                mDotRadius = arr.getDimension(R.styleable.MoreActionView_more_action_dot_radius, mDotRadius);
            }

            if (arr.hasValue(R.styleable.MoreActionView_more_action_dot_span)) {
                mDotSpan = (int) arr.getDimension(R.styleable.MoreActionView_more_action_dot_span, mDotSpan);
            }

            mColor = arr.getColor(R.styleable.MoreActionView_more_action_dot_color, mColor);
            arr.recycle();
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredHeight = (int) (mDotRadius * 2);
        int desiredWidth = (int) (mDotRadius * 6 + mDotSpan * 2);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            // Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            // Be whatever you want
            width = desiredWidth;
        }

        // Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            // Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            // Be whatever you want
            height = desiredHeight;
        }

        // MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public void setColorFilter(int color) {
        setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public final void setColorFilter(int color, PorterDuff.Mode mode) {
        mPaint.setColorFilter(new PorterDuffColorFilter(color, mode));
        invalidate();
    }

    public final void clearColorFilter() {
        mPaint.setColorFilter(null);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        for (int i = 0; i < 3; i++) {
            float x = i * (mDotRadius * 2 + mDotSpan) + mDotRadius;
            canvas.drawCircle(x, mDotRadius, mDotRadius, mPaint);
        }
    }
}
