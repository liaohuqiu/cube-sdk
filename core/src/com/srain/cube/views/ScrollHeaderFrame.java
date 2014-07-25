package com.srain.cube.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

import com.srain.cube.R;

/**
 * @author http://www.liaohuqiu.net
 */
public class ScrollHeaderFrame extends FrameLayout {

    private static final boolean DEBUG = true;
    private static final String LOG_TAG = ScrollHeaderFrame.class.getName();

    private int mHeaderHeight;
    private int mCurrentTop = 0;
    private int mHeaderId = 0;
    private int mContainerId = 0;

    private ViewGroup mContentViewContainer;
    private View mHeaderContainer;

    private PointF mPtLastMove = new PointF();

    public ScrollHeaderFrame(Context context) {
        this(context, null);
    }

    public ScrollHeaderFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollHeaderFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ScrollHeaderFrame, 0, 0);
        if (arr != null) {
            if (arr.hasValue(R.styleable.ScrollHeaderFrame_scrollheaderframe_header)) {
                mHeaderId = arr.getResourceId(R.styleable.ScrollHeaderFrame_scrollheaderframe_header, 0);
            }
            if (arr.hasValue(R.styleable.ScrollHeaderFrame_scrollheaderframe_conent_container)) {
                mContainerId = arr.getResourceId(R.styleable.ScrollHeaderFrame_scrollheaderframe_conent_container, 0);
            }
            arr.recycle();
        }
    }

    public View getContentView() {
        return mContentViewContainer;
    }

    public View getHeaderView() {
        return mHeaderContainer;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHeaderContainer = findViewById(mHeaderId);
        mContentViewContainer = (ViewGroup) findViewById(mContainerId);

        setDrawingCacheEnabled(false);
        setBackgroundDrawable(null);
        setClipChildren(false);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (DEBUG) {
            Log.d(LOG_TAG, String.format("onLayout: %s, %s %s %s", l, t, r, b));
        }

        int headerHeight = mHeaderContainer.getMeasuredHeight();
        if (headerHeight == 0) {
            return;
        }

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();


        int lastTop = mHeaderContainer.getTop();
        int change = mCurrentTop - lastTop;
        mHeaderContainer.offsetTopAndBottom(change);
        mContentViewContainer.offsetTopAndBottom(change);

        int containerOffset = headerHeight + mCurrentTop;
        mContentViewContainer.layout(0, containerOffset, w, h);

        if (DEBUG) {
            Log.d(LOG_TAG, String.format("onLayout: w:%s h:%s mCurrentTop: %s change:%s containerOffset: %s", w, h, mCurrentTop, change, containerOffset));
        }
    }

    /**
     * if deltaY > 0, tryToMove the content up
     */
    private boolean tryToMoveAndCheckContainerCanMove(float deltaY) {

        // has reached the top
        if ((deltaY > 0 && mCurrentTop == -mHeaderHeight)) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("has reached the top"));
            }
            return true;
        }

        // has reached the bottom
        if ((deltaY < 0 && mCurrentTop == 0)) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("has reached the bottom"));
            }
            return true;
        }

        int to = mCurrentTop - (int) deltaY;

        boolean reached = false;

        // over top
        if (to < -mHeaderHeight) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("over top"));
            }
            reached = true;
            to = -mHeaderHeight;
        }

        // over bottom
        if (to > 0) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("over bottom"));
            }
            reached = true;
            to = 0;
        }
        moveTo(to);
        return reached;
    }

    private void moveTo(int to) {
        if (DEBUG) {
            Log.d(LOG_TAG, String.format("moveTo: %s %s, %s", to, mCurrentTop, mHeaderHeight));
        }
        if (mCurrentTop == to) {
            return;
        }
        mCurrentTop = to;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        mHeaderHeight = mHeaderContainer.getMeasuredHeight();

        h = h - (mHeaderHeight + mCurrentTop);

        if (DEBUG) {
            Log.d(LOG_TAG, String.format("onMeasure %s getMeasuredHeight: %s, %s", h, mHeaderContainer.getMeasuredHeight(), this));
        }

        mContentViewContainer.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {

        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_DOWN:
                mPtLastMove.set(e.getX(), e.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (mHeaderHeight == 0) {
                    break;
                }

                float offsetX = mPtLastMove.x - e.getX();
                float deltaY = (int) (mPtLastMove.y - e.getY());
                mPtLastMove.set(e.getX(), e.getY());
                boolean moveUp = deltaY > 0;
                boolean canMoveUp = mCurrentTop > -mHeaderHeight;
                boolean moveDown = !moveUp;
                boolean canMoveDown = mCurrentTop < 0;
                if (DEBUG) {
                    Log.d(LOG_TAG, String.format("ACTION_MOVE: %s, moveUp: %s, canMoveUp: %s, moveDown: %s, canMoveDown: %s %s", deltaY, moveUp, canMoveUp, moveDown, canMoveDown, this));
                }
                if ((moveUp && canMoveUp) || (moveDown && canMoveDown)) {
                    boolean containerCanMove = tryToMoveAndCheckContainerCanMove(deltaY);
                    if (!containerCanMove) {
                        return true;
                    }
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(e);
    }
}