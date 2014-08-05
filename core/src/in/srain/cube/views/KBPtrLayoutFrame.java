package in.srain.cube.views;

import android.content.Context;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

public abstract class KBPtrLayoutFrame extends FrameLayout {

    // ===========================================================
    // Interface
    // ===========================================================
    public interface OnRefreshHandler {
        public void onRefresh();
    }

    /**
     * if content is empty or the first child is in view, should do refresh
     * after release
     */
    protected abstract Boolean contentIsEmptyOrFirstChildInView();

    /**
     * if the item in the content is long pressing, cancel move
     */
    protected abstract Boolean contentItemIsLongPressing();

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

    private LinearLayout mHeaderContainer;
    private FrameLayout mHeader;
    private ImageView mArrowImage;

    private State mState;
    private int mHeaderHeight;
    private View mContentView;

    private GestureDetector mDetector;
    private FlingRunnable mFlinger;
    private int mDestPading;
    private int mLastTop;
    private int mPagingTouchSlop;

    private OnRefreshHandler mContentProvider;

    private MotionEvent mDownEvent;
    private PointF mPtLastMove = new PointF();

    private CheckForLongPress mPendingCheckForLongPress = new CheckForLongPress();
    private CheckForLongPress2 mPendingCheckForLongPress2 = new CheckForLongPress2();
    private float lastY;

    private boolean mlistviewDoScrollL = false;
    private boolean mLongPressing;
    private boolean mPendingRemoved = false;

    private boolean isRefreshing = false;

    private boolean mPreventForHorizontal = false;

    public KBPtrLayoutFrame(Context context, View contentView) {
        super(context);
        mContentView = contentView;
        mFlinger = new FlingRunnable();
        mDetector = new GestureDetector(context, new MyOnGestureListener());
        doLayout();
    }

    protected void doLayout() {

        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;

        setDrawingCacheEnabled(false);
        setBackgroundDrawable(null);
        setClipChildren(false);
        mDetector.setIsLongpressEnabled(false);

        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
        mReverseFlipAnimation.setFillAfter(true);

/*		mHeaderContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.ptr_header, null);
        mHeader = (FrameLayout) mHeaderContainer.findViewById(R.id.ptr_id_header);
		mArrowImage = (ImageView) mHeader.findViewById(R.id.ptr_id_image);*/
        ViewTreeObserver vto = mHeader.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new PTROnGlobalLayoutListener());

        addView(mHeaderContainer);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(layoutParams);
        addView(mContentView);
    }

    @Override
    protected void onLayout(boolean flag, int i, int j, int k, int l) {

        mLastTop = -mHeaderHeight;

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        mHeaderContainer.layout(0, -mHeaderHeight, w, 0);
        mContentView.layout(0, 0, w, h);
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

    private void moveUp(float deltaY) {
        mHeaderContainer.offsetTopAndBottom((int) -deltaY);
        mContentView.offsetTopAndBottom((int) -deltaY);
    }

    private void updateArrowImage() {
        if (mHeaderContainer.getTop() < HEADER_OFFSET_TO_TOP && mLastTop >= HEADER_OFFSET_TO_TOP) {
            mArrowImage.clearAnimation();
            mArrowImage.startAnimation(mReverseFlipAnimation);
        } else if (mHeaderContainer.getTop() > HEADER_OFFSET_TO_TOP && mLastTop <= HEADER_OFFSET_TO_TOP) {
            mArrowImage.clearAnimation();
            mArrowImage.startAnimation(mFlipAnimation);
        }
        mLastTop = mHeaderContainer.getTop();
    }

    private boolean release() {
        if (mlistviewDoScrollL) {
            mlistviewDoScrollL = false;
            return true;
        }
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
        if (mContentProvider != null) {
            if (!entryLock())
                return;
            mContentProvider.onRefresh();
        }
    }

    public void onRefreshComplete() {
        unlock();
    }

    public View getContent() {
        return mContentView;
    }

    public boolean dispatchTouchEvent(MotionEvent e) {
        int action;
        float y = e.getY();
        action = e.getAction();
        if (mLongPressing && action != MotionEvent.ACTION_DOWN) {
            return false;
        }
        boolean handled = true;
        handled = mDetector.onTouchEvent(e);
        switch (action) {
            case MotionEvent.ACTION_UP:
                boolean f1 = mContentView.getTop() <= e.getY() && e.getY() <= mContentView.getBottom();
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
                mLongPressing = false;
                postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout() + 100);
                mPendingRemoved = false;
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
                if (!mPendingRemoved) {
                    removeCallbacks(mPendingCheckForLongPress);
                    mPendingRemoved = true;
                }
                if (!handled && mHeaderContainer.getTop() == -mHeaderHeight) {
                    try {
                        return super.dispatchTouchEvent(e);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        return true;
                    }
                } else if (handled && mContentView.getTop() > 0 && deltaY < 0) {
                    e.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(e);
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void setOnRefreshHandler(OnRefreshHandler handler) {
        mContentProvider = handler;
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
            boolean flag = contentIsEmptyOrFirstChildInView();
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

    private class PTROnGlobalLayoutListener implements OnGlobalLayoutListener {

        @SuppressWarnings("deprecation")
        public void onGlobalLayout() {
            int initialHeaderHeight = mHeader.getHeight();

            if (initialHeaderHeight > 0) {
                mHeaderHeight = initialHeaderHeight;
                requestLayout();
            }
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }

    /**
     * if the some of the items in the content, set mLongPressing to true
     */
    private class CheckForLongPress implements Runnable {
        public void run() {
            if (contentItemIsLongPressing()) {
                postDelayed(mPendingCheckForLongPress2, 100);
            }
        }
    }

    private class CheckForLongPress2 implements Runnable {
        public void run() {
            mLongPressing = true;
            MotionEvent e = MotionEvent.obtain(mDownEvent.getDownTime(), mDownEvent.getEventTime() + ViewConfiguration.getLongPressTimeout(), MotionEvent.ACTION_CANCEL, mDownEvent.getX(), mDownEvent.getY(), mDownEvent.getMetaState());
            KBPtrLayoutFrame.super.dispatchTouchEvent(e);
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