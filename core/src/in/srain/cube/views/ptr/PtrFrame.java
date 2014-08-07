package in.srain.cube.views.ptr;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.Scroller;
import in.srain.cube.R;
import in.srain.cube.util.CLog;

public class PtrFrame extends FrameLayout {

    private static final boolean DEBUG = CLog.DEBUG_PTR_FRAME;
    private static final String LOG_TAG = "PtrFrame";

    // ===========================================================
    // Interface
    // ===========================================================
    public interface PtrHandler {
        /**
         * if content is empty or the first child is in view, should do refresh
         * after release
         */
        public boolean canDoRefresh();

        public void onRefresh();

        public void crossRotateLineFromTop();

        public void crossRotateLineFromBottom();
    }

    // ===========================================================
    // enumeration
    // ===========================================================
    public enum State {
        FREE, PREPARE_REFRESH
    }

    // ===========================================================
    // Fields
    // ===========================================================
    private int mCloseDelay = 300;
    private double mResistance = 1.5;
    private int mRotateAniTime = 250;
    private int mOffsetToRotateView = 0;
    private float mRationOfHeightToRotate = 1.2f;

    private int mHeaderId = 0;
    private int mContainerId = 0;
    private int mRotateViewId = 0;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    private View mHeaderContainer;
    private View mRotateView;
    private ViewGroup mContentViewContainer;

    private State mState;
    private int mHeaderHeight;

    private FlingRunnable mFlingRunnable;

    private int mCurrentPos = 0;
    private int mLastPos = 0;
    private int mPagingTouchSlop;

    private PtrHandler mPtrHandler;

    private PointF mPtLastMove = new PointF();

    private boolean isRefreshing = false;

    private boolean mPreventForHorizontal = false;

    public PtrFrame(Context context) {
        this(context, null);
    }

    public PtrFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PtrFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PtrFrame, 0, 0);
        if (arr != null) {
            if (arr.hasValue(R.styleable.PtrFrame_ptr_header)) {
                mHeaderId = arr.getResourceId(R.styleable.PtrFrame_ptr_header, 0);
            }
            if (arr.hasValue(R.styleable.PtrFrame_ptr_content)) {
                mContainerId = arr.getResourceId(R.styleable.PtrFrame_ptr_content, 0);
            }
            if (arr.hasValue(R.styleable.PtrFrame_ptr_rotate_view)) {
                mRotateViewId = arr.getResourceId(R.styleable.PtrFrame_ptr_rotate_view, 0);
            }

            if (arr.hasValue(R.styleable.PtrFrame_ptr_close_delay)) {
                mCloseDelay = arr.getInt(R.styleable.PtrFrame_ptr_close_delay, 300);
            }
            if (arr.hasValue(R.styleable.PtrFrame_ptr_resistance)) {
                mResistance = arr.getFloat(R.styleable.PtrFrame_ptr_resistance, 1.5f);
            }
            if (arr.hasValue(R.styleable.PtrFrame_ptr_rotate_ani_time)) {
                mRotateAniTime = arr.getInt(R.styleable.PtrFrame_ptr_rotate_ani_time, 250);
            }
            mRationOfHeightToRotate = arr.getFloat(R.styleable.PtrFrame_ptr_ratio_of_header_to_rotate, 0.9f);
            arr.recycle();
        }

        mFlingRunnable = new FlingRunnable();

        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;

        setDrawingCacheEnabled(false);
        setBackgroundDrawable(null);
        setClipChildren(false);

        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(mRotateAniTime);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(mRotateAniTime);
        mReverseFlipAnimation.setFillAfter(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHeaderContainer = findViewById(mHeaderId);
        mContentViewContainer = (ViewGroup) findViewById(mContainerId);
        mRotateView = findViewById(mRotateViewId);
    }

    @Override
    protected void onLayout(boolean flag, int i, int j, int k, int l) {

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        if (DEBUG) {
            CLog.d(LOG_TAG, "onLayout %s", mHeaderHeight);
        }
        mHeaderContainer.layout(0, -mHeaderHeight, w, 0);
        mContentViewContainer.layout(0, 0, w, h);
    }

    /**
     * if deltaY > 0, move the content down
     */
    private boolean move(float deltaY) {

        // has reached the top
        if ((deltaY < 0 && mCurrentPos == 0)) {
            if (DEBUG) {
                CLog.d(LOG_TAG, String.format("has reached the top"));
            }
            return true;
        }

        int to = mCurrentPos + (int) deltaY;

        // over top
        if (to < 0) {
            if (DEBUG) {
                CLog.d(LOG_TAG, String.format("over top"));
            }
            to = 0;
        }

        mLastPos = mCurrentPos;
        mCurrentPos = to;
        invalidate();
        updatePos();
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderContainer != null) {
            mHeaderHeight = mHeaderContainer.getMeasuredHeight();
            mCurrentPos = mHeaderContainer.getBottom();
            mOffsetToRotateView = (int) (mHeaderHeight * mRationOfHeightToRotate);
        }

        if (DEBUG) {
            CLog.d(LOG_TAG, "onMeasure, mHeaderHeight: %s, mCurrentPos: %s", mHeaderHeight, mCurrentPos);
        }
    }

    private void updatePos() {
        int lastPos = mHeaderContainer.getBottom();
        int change = mCurrentPos - lastPos;
        mHeaderContainer.offsetTopAndBottom(change);
        mContentViewContainer.offsetTopAndBottom(change);

        if (mCurrentPos < mOffsetToRotateView && mLastPos >= mOffsetToRotateView) {
            if (null != mPtrHandler) {
                mPtrHandler.crossRotateLineFromBottom();
            }
            mRotateView.clearAnimation();
            mRotateView.startAnimation(mReverseFlipAnimation);
        } else if (mCurrentPos > mOffsetToRotateView && mLastPos <= mOffsetToRotateView) {
            if (null != mPtrHandler) {
                mPtrHandler.crossRotateLineFromTop();
            }
            mRotateView.clearAnimation();
            mRotateView.startAnimation(mFlipAnimation);
        }
    }

    private boolean release() {
        if (mHeaderContainer.getTop() > mOffsetToRotateView) {
            mState = State.PREPARE_REFRESH;
        }
        mFlingRunnable.startUsingDistance();
        return false;
    }

    protected boolean entryLock() {
        if (isRefreshing) {
            return false;
        }
        isRefreshing = true;
        return true;
    }

    protected void unlock() {
        isRefreshing = false;
    }

    private void notifyRefresh() {
        if (mPtrHandler != null) {
            if (!entryLock())
                return;
            mPtrHandler.onRefresh();
        }
    }

    public void onRefreshComplete() {
        unlock();
    }

    public View getContent() {
        return mContentViewContainer;
    }

    public boolean dispatchTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrentPos > 0) {
                    release();
                    return true;
                } else {
                    return super.dispatchTouchEvent(e);
                }

            case MotionEvent.ACTION_DOWN:
                mPtLastMove.set(e.getX(), e.getY());
                mPreventForHorizontal = false;
                return super.dispatchTouchEvent(e);

            case MotionEvent.ACTION_MOVE:
                float offsetX = e.getX() - mPtLastMove.x;
                float offsetY = (int) (e.getY() - mPtLastMove.y);
                mPtLastMove.set(e.getX(), e.getY());
                if (!mPreventForHorizontal && (Math.abs(offsetX) > mPagingTouchSlop || Math.abs(offsetX) > 2 * Math.abs(offsetY))) {
                    if (frameIsMoved()) {
                        mPreventForHorizontal = true;
                    }
                }
                if (mPreventForHorizontal) {
                    return super.dispatchTouchEvent(e);
                }

                mFlingRunnable.checkStop();

                boolean moveDown = offsetY > 0;
                boolean moveUp = !moveDown;
                boolean canMoveUp = mCurrentPos > 0;

                if (DEBUG) {
                    CLog.d(LOG_TAG, "ACTION_MOVE: offsetY:%s, mCurrentPos: %s, moveUp: %s, canMoveUp: %s, moveDown: %s", offsetY, mCurrentPos, moveUp, canMoveUp, moveDown);
                }

                // disable move when header not reach top
                if (moveDown && mPtrHandler != null && !mPtrHandler.canDoRefresh()) {
                    return super.dispatchTouchEvent(e);
                }

                if ((moveUp && canMoveUp) || moveDown) {
                    offsetY = (float) ((double) offsetY / mResistance);
                    move(offsetY);
                    return true;
                }
        }
        return super.dispatchTouchEvent(e);
    }

    public void setPtrHandler(PtrHandler ptrHandler) {
        mPtrHandler = ptrHandler;
    }

    private boolean frameIsMoved() {
        return mLastPos == -mHeaderHeight;
    }

    class FlingRunnable implements Runnable {

        private int mLastFlingY;
        private Scroller mScroller;
        private boolean mIsRunning = false;

        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }

        public void run() {
            boolean noFinish = mScroller.computeScrollOffset() && !mScroller.isFinished();
            CLog.d(LOG_TAG, "mIsRunning: %s, noFinish: %s", mIsRunning, noFinish);
            if (noFinish) {
                int curY = mScroller.getCurrY();
                int deltaY = curY - mLastFlingY;
                move(deltaY);
                if (mCurrentPos == 0 && mState == State.PREPARE_REFRESH) {
                    mState = State.FREE;
                    notifyRefresh();
                }
                mLastFlingY = curY;
                post(this);
            } else {
                removeCallbacks(this);
                mState = State.FREE;
                mIsRunning = false;
            }
        }

        public void checkStop() {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            if (mIsRunning) {
                removeCallbacks(this);
                mIsRunning = false;
            }
        }

        public void startUsingDistance() {
            if (mCurrentPos == 0) {
                return;
            }
            int distance = -mCurrentPos;
            removeCallbacks(this);
            mLastFlingY = 0;
            mScroller.startScroll(0, 0, 0, distance, mCloseDelay);
            post(this);
            mIsRunning = true;
        }
    }
}