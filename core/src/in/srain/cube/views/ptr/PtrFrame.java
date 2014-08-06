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

    private static final boolean DEBUG = CLog.DEBUG_SCROLL_HEADER_FRAME;
    private static final String LOG_TAG = PtrFrame.class.getName();

    // ===========================================================
    // Interface
    // ===========================================================
    public interface RefreshHandler {
        public void onRefresh();
    }

    public interface ContentChecker {
        /**
         * if content is empty or the first child is in view, should do refresh
         * after release
         */
        public boolean contentIsEmptyOrFirstChildInView();

        /**
         * if the item in the content is long pressing, cancel move
         */
        public boolean contentItemIsLongPressing();
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
    public static int HEADER_OFFSET_TO_TOP = 20;
    public static final double PULL_RESISTANCE = 1.2d;
    private static final int CLOSEDELAY = 300;

    private static final int ROTATE_ARROW_ANIMATION_DURATION = 250;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    private int mHeaderId = 0;
    private int mContainerId = 0;
    private int mReverseViewId = 0;

    private ViewGroup mContentViewContainer;
    private View mHeaderContainer;
    private View mReverseView;

    private State mState;
    private int mHeaderHeight;

    private GestureDetector mDetector;
    private FlingRunnable mFlinger;
    private int mDestPading;
    private int mLastTop;
    private int mPagingTouchSlop;

    private RefreshHandler mRefreshHandler;
    private ContentChecker mContentChecker;

    private MotionEvent mDownEvent;
    private PointF mPtLastMove = new PointF();

    private float lastY;

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
            if (arr.hasValue(R.styleable.PtrFrame_ptr_reverse_view)) {
                mReverseViewId = arr.getResourceId(R.styleable.PtrFrame_ptr_reverse_view, 0);
            }
            arr.recycle();
        }

        mFlinger = new FlingRunnable();
        mDetector = new GestureDetector(context, new MyOnGestureListener());
        mDetector.setIsLongpressEnabled(false);

        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;

        setDrawingCacheEnabled(false);
        setBackgroundDrawable(null);
        setClipChildren(false);

        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
        mReverseFlipAnimation.setFillAfter(true);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHeaderContainer = findViewById(mHeaderId);
        mContentViewContainer = (ViewGroup) findViewById(mContainerId);
        mReverseView = findViewById(mReverseViewId);
    }

    @Override
    protected void onLayout(boolean flag, int i, int j, int k, int l) {

        mLastTop = -mHeaderHeight;

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        mHeaderContainer.layout(0, -mHeaderHeight, w, 0);
        mContentViewContainer.layout(0, 0, w, h);
    }

    /**
     * if deltaY > 0, move the content up
     */
    private boolean move(float deltaY, boolean auto) {
        // has reach the top
        if (deltaY > 0 && mHeaderContainer.getTop() == -mHeaderHeight) {
            return false;
        }
        if (auto) {
            // in case of move over destination
            if (mHeaderContainer.getTop() - deltaY < mDestPading) {
                deltaY = mHeaderContainer.getTop() - mDestPading;
            }
            moveUp(deltaY);
            if (mDestPading == mHeaderContainer.getTop() && mState == State.PREPARE_REFRESH) {
                mState = State.FREE;
                onRefresh();
            }
            invalidate();
            updateArrowImage();
            return true;
        } else {
            moveUp(deltaY);
        }

        // move content up, but the start position of list is above the top
        // start position of the screen
        // move back to the begin position.
        if (mHeaderContainer.getTop() <= -mHeaderHeight) {
            // now => target
            deltaY = mHeaderContainer.getTop() - (-mHeaderHeight);
            moveUp(deltaY);
            updateArrowImage();
            invalidate();
            return false;
        }
        updateArrowImage();
        invalidate();
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderContainer != null) {
            mHeaderHeight = mHeaderContainer.getMeasuredHeight();
        }

        if (DEBUG) {
            CLog.d(LOG_TAG, "onMeasure, mHeaderHeight: %s", mHeaderHeight);
        }
    }

    private void moveUp(float deltaY) {
        mHeaderContainer.offsetTopAndBottom((int) -deltaY);
        mContentViewContainer.offsetTopAndBottom((int) -deltaY);
    }

    private void updateArrowImage() {
        if (mHeaderContainer.getTop() < HEADER_OFFSET_TO_TOP && mLastTop >= HEADER_OFFSET_TO_TOP) {
            mReverseView.clearAnimation();
            mReverseView.startAnimation(mReverseFlipAnimation);
        } else if (mHeaderContainer.getTop() > HEADER_OFFSET_TO_TOP && mLastTop <= HEADER_OFFSET_TO_TOP) {
            mReverseView.clearAnimation();
            mReverseView.startAnimation(mFlipAnimation);
        }
        mLastTop = mHeaderContainer.getTop();
    }

    private boolean release() {
        if (mHeaderContainer.getTop() > HEADER_OFFSET_TO_TOP) {
            mState = State.PREPARE_REFRESH;
        }
        scrollToClose();
        invalidate();
        return false;
    }

    private void scrollToClose() {
        mDestPading = -mHeaderHeight;
        mFlinger.startUsingDistance(mHeaderHeight + mHeaderContainer.getTop(), CLOSEDELAY);
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

    private void onRefresh() {
        if (mRefreshHandler != null) {
            if (!entryLock())
                return;
            mRefreshHandler.onRefresh();
        }
    }

    public void onRefreshComplete() {
        unlock();
    }

    public View getContent() {
        return mContentViewContainer;
    }

    public boolean dispatchTouchEvent(MotionEvent e) {
        int action;
        float y = e.getY();
        action = e.getAction();
        boolean handled = mDetector.onTouchEvent(e);
        switch (action) {
            case MotionEvent.ACTION_UP:
                boolean f1 = mContentViewContainer.getTop() <= e.getY() && e.getY() <= mContentViewContainer.getBottom();
                if (!handled && mHeaderContainer.getTop() == -mHeaderHeight && f1) {
                    super.dispatchTouchEvent(e);
                } else {
                    handled = release();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                handled = release();
                super.dispatchTouchEvent(e);
                break;
            case MotionEvent.ACTION_DOWN:

                mPtLastMove.set(e.getX(), e.getY());
                mDownEvent = e;
                mPreventForHorizontal = false;
                super.dispatchTouchEvent(e);
                break;
            case MotionEvent.ACTION_MOVE:

                float offsetX = mPtLastMove.x - e.getX();
                float offsetY = (int) (mPtLastMove.y - e.getY());

                if (Math.abs(offsetX) > mPagingTouchSlop || Math.abs(offsetX) > 2 * Math.abs(offsetY)) {
                    if (frameIsMoved()) {
                        mPreventForHorizontal = true;
                    }
                }
                if (mPreventForHorizontal) {
                    return super.dispatchTouchEvent(e);
                }
                float deltaY = lastY - y;
                lastY = y;
                if (!handled && mHeaderContainer.getTop() == -mHeaderHeight) {
                    try {
                        return super.dispatchTouchEvent(e);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        return true;
                    }
                } else if (handled && mContentViewContainer.getTop() > 0 && deltaY < 0) {
                    e.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(e);
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void setRefreshHandler(RefreshHandler handler) {
        mRefreshHandler = handler;
    }

    public void setContentChecker(ContentChecker contentChecker) {
        mContentChecker = contentChecker;
    }

    // ===========================================================
    // Inner class
    // ===========================================================
    private class MyOnGestureListener implements GestureDetector.OnGestureListener {

        public boolean onDown(MotionEvent e) {
            return false;
        }

        public boolean onFling(MotionEvent motionevent, MotionEvent e, float f, float f1) {
            return false;
        }

        public void onLongPress(MotionEvent e) {
        }

        /**
         * if deltaY > 0, the finger is move forward to the top of the screen
         */
        public boolean onScroll(MotionEvent curdown, MotionEvent cur, float deltaX, float deltaY) {
            if (mPreventForHorizontal)
                return false;
            deltaY = (float) ((double) deltaY / PULL_RESISTANCE);
            boolean handled = false;
            boolean flag = mContentChecker != null && mContentChecker.contentIsEmptyOrFirstChildInView();
            if (deltaY < 0F && flag || mHeaderContainer.getTop() > -mHeaderHeight) {
                handled = move(deltaY, false);
            } else
                handled = false;
            return handled;
        }

        public void onShowPress(MotionEvent motionevent) {
        }

        public boolean onSingleTapUp(MotionEvent motionevent) {
            return false;
        }
    }

    private boolean frameIsMoved() {
        return mLastTop == -mHeaderHeight;
    }

    class FlingRunnable implements Runnable {

        private int mLastFlingY;
        private Scroller mScroller;

        public FlingRunnable() {
            mScroller = new Scroller(getContext());
        }

        public void run() {
            boolean noFinish = mScroller.computeScrollOffset();
            if (noFinish) {
                int curY = mScroller.getCurrY();
                int deltaY = curY - mLastFlingY;
                move(deltaY, true);
                mLastFlingY = curY;
                post(this);
            } else {
                removeCallbacks(this);
                mState = State.FREE;
            }
        }

        public void startUsingDistance(int distance, int duration) {
            if (distance == 0)
                distance--;
            removeCallbacks(this);
            mLastFlingY = 0;
            mScroller.startScroll(0, 0, 0, distance, duration);
            post(this);
        }
    }
}