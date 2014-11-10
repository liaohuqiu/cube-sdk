package in.srain.cube.views.ptr;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import in.srain.cube.R;
import in.srain.cube.util.CLog;
import in.srain.cube.util.Debug;
import in.srain.cube.util.Version;

public class PtrFrame extends RelativeLayout {

    private static final boolean DEBUG = Debug.DEBUG_PTR_FRAME;
    private static int ID = 1;
    private final String LOG_TAG = "PtrFrame" + ++ID;

    // ===========================================================
    // Interface
    // ===========================================================
    public interface PtrHandler {
        /**
         * if content is empty or the first child is in view, should do refresh
         * after release
         */
        public boolean checkCanDoRefresh(PtrFrame frame, View content, View header);

        public void onRefresh();

        public void onBackToTop();

        public void onRelease();

        public void onRefreshComplete();

        public void crossRotateLineFromTop(boolean isInTouching);

        public void onPercentageChange(int oldPosition, int newPosition, float oldPercent, float newPercent);

        public void crossRotateLineFromBottom(boolean isInTouching);
    }

    public static abstract class DefaultPtrHandler implements PtrHandler {

        @Override
        public boolean checkCanDoRefresh(PtrFrame frame, View content, View header) {
            return checkCanScrollUp(frame, content, header);
        }
    }

    public static boolean checkCanScrollUp(PtrFrame frame, View content, View header) {
        if (!(content instanceof ViewGroup)) {
            return true;
        }

        ViewGroup viewGroup = (ViewGroup) content;
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (content instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) content;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return content.getScrollY() > 0;
            }
        } else {
            if (viewGroup.getChildCount() == 0) {
                return true;
            }
            return viewGroup.getChildAt(0).getTop() == 0;
        }
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
    private int mHeaderId = 0;
    private int mContainerId = 0;
    private int mRotateViewId = 0;

    private float mResistance = 1.5f;

    private int mDurationClose = 300;
    private int mDurationToCloseHeader = 700;
    private int mRotateAniTime = 150;

    private float mRatioOfHeaderToRotate = 1.5f;
    private float mRatioOfHeaderToRefresh = 1.5f;
    private boolean mKeepHeaderWhenRefresh = true;

    private int mOffsetToRotateView = 0;
    private int mOffsetToRefresh = 0;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    protected View mRotateView;
    protected View mHeaderContainer;
    protected View mContentViewContainer;

    private State mState;
    private int mHeaderHeight;

    private ScrollChecker mScrollChecker;

    private int mCurrentPos = 0;
    private int mLastPos = 0;
    private int mPagingTouchSlop;

    private PtrHandler mPtrHandler;

    private PointF mPtLastMove = new PointF();

    private boolean mIsRefreshing = false;
    private boolean mPreventForHorizontal = false;
    private boolean mIsInTouching = false;
    private boolean mDisableWhenHorizontalMove = false;

    public PtrFrame(Context context) {
        this(context, null);
    }

    public PtrFrame(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PtrFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (DEBUG) {
            CLog.d(LOG_TAG, "PtrFrame(Context context, AttributeSet attrs, int defStyle)");
        }

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.PtrFrame, 0, 0);
        if (arr != null) {

            mHeaderId = arr.getResourceId(R.styleable.PtrFrame_ptr_header, mHeaderId);
            mContainerId = arr.getResourceId(R.styleable.PtrFrame_ptr_content, mContainerId);
            mRotateViewId = arr.getResourceId(R.styleable.PtrFrame_ptr_rotate_view, mRotateViewId);

            mResistance = arr.getFloat(R.styleable.PtrFrame_ptr_resistance, mResistance);

            mDurationClose = arr.getInt(R.styleable.PtrFrame_ptr_duration_to_close, mDurationClose);
            mDurationToCloseHeader = arr.getInt(R.styleable.PtrFrame_ptr_duration_to_close_header, mDurationToCloseHeader);
            mRotateAniTime = arr.getInt(R.styleable.PtrFrame_ptr_rotate_ani_time, mRotateAniTime);

            mRatioOfHeaderToRotate = arr.getFloat(R.styleable.PtrFrame_ptr_ratio_of_header_to_rotate, mRatioOfHeaderToRotate);
            mRatioOfHeaderToRefresh = arr.getFloat(R.styleable.PtrFrame_ptr_ratio_of_header_to_refresh, mRatioOfHeaderToRefresh);
            mKeepHeaderWhenRefresh = arr.getBoolean(R.styleable.PtrFrame_ptr_keep_header_when_refresh, mKeepHeaderWhenRefresh);
            arr.recycle();
        }

        mScrollChecker = new ScrollChecker();

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

        if (mHeaderId != 0) {
            mHeaderContainer = findViewById(mHeaderId);
        }
        if (mContainerId != 0) {
            mContentViewContainer = findViewById(mContainerId);
        }
        if (mRotateViewId != 0) {
            mRotateView = findViewById(mRotateViewId);
        }

        if (DEBUG) {
            CLog.d(LOG_TAG, "onFinishInflate");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderContainer != null && mHeaderHeight != mHeaderContainer.getMeasuredHeight()) {
            mHeaderHeight = mHeaderContainer.getMeasuredHeight();
            mCurrentPos = mHeaderContainer.getBottom();
            mOffsetToRotateView = (int) (mHeaderHeight * mRatioOfHeaderToRotate);
            mOffsetToRefresh = (int) (mHeaderHeight * mRatioOfHeaderToRefresh);
        }

        if (DEBUG) {
            CLog.d(LOG_TAG, "onMeasure, mHeaderHeight: %s, mCurrentPos: %s", mHeaderHeight, mCurrentPos);
        }
    }

    @Override
    protected void onLayout(boolean flag, int i, int j, int k, int l) {

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();

        if (DEBUG) {
            CLog.d(LOG_TAG, "onLayout mHeaderHeight: %s, mCurrentPos: %s", mHeaderHeight, mCurrentPos);
        }

        if (Version.hasHoneycomb()) {
            if (mHeaderContainer != null) {
                mHeaderContainer.layout(0, -mHeaderHeight + mCurrentPos, w, mCurrentPos);
            }
            if (mContentViewContainer != null) {
                mContentViewContainer.layout(0, mCurrentPos, w, h + mCurrentPos);
            }
        } else {
            if (mHeaderContainer != null) {
                mHeaderContainer.layout(0, -mHeaderHeight + mCurrentPos, w, mCurrentPos);
            }
            if (mContentViewContainer != null) {
                mContentViewContainer.layout(0, mCurrentPos, w, h + mCurrentPos);
            }
            // FrameLayout.LayoutParams lyp1 = new LayoutParams(w, mHeaderHeight);
            // mHeaderContainer.setLayoutParams(lyp1);

            // FrameLayout.LayoutParams lyp2 = new LayoutParams(w, h - mHeaderHeight);
            // lyp2.setMargins(0, mHeaderHeight, 0, 0);
            // mHeaderContainer.setLayoutParams(lyp2);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsInTouching = false;
                if (mCurrentPos > 0) {
                    release();
                    return true;
                } else {
                    return super.dispatchTouchEvent(e);
                }

            case MotionEvent.ACTION_DOWN:
                mIsInTouching = true;
                mPtLastMove.set(e.getX(), e.getY());
                mPreventForHorizontal = false;
                return super.dispatchTouchEvent(e);

            case MotionEvent.ACTION_MOVE:
                float offsetX = e.getX() - mPtLastMove.x;
                float offsetY = (int) (e.getY() - mPtLastMove.y);
                mPtLastMove.set(e.getX(), e.getY());
                if (mDisableWhenHorizontalMove && !mPreventForHorizontal && (Math.abs(offsetX) > mPagingTouchSlop || Math.abs(offsetX) > 3 * Math.abs(offsetY))) {
                    if (frameIsNotMoved()) {
                        mPreventForHorizontal = true;
                    }
                }
                if (mPreventForHorizontal) {
                    return super.dispatchTouchEvent(e);
                }

                mScrollChecker.abortIfWorking();

                boolean moveDown = offsetY > 0;
                boolean moveUp = !moveDown;
                boolean canMoveUp = mCurrentPos > 0;

                if (DEBUG) {
                    CLog.d(LOG_TAG, "ACTION_MOVE: offsetY:%s, mCurrentPos: %s, moveUp: %s, canMoveUp: %s, moveDown: %s", offsetY, mCurrentPos, moveUp, canMoveUp, moveDown);
                }

                // disable move when header not reach top
                if (moveDown && mPtrHandler != null && !mPtrHandler.checkCanDoRefresh(this, mContentViewContainer, mHeaderContainer)) {
                    return super.dispatchTouchEvent(e);
                }

                if ((moveUp && canMoveUp) || moveDown) {
                    offsetY = (float) ((double) offsetY / mResistance);
                    movePos(offsetY);
                    return true;
                }
        }
        return super.dispatchTouchEvent(e);
    }

    /**
     * if deltaY > 0, move the content down
     */
    private void movePos(float deltaY) {
        // has reached the top
        if ((deltaY < 0 && mCurrentPos == 0)) {
            if (DEBUG) {
                CLog.e(LOG_TAG, String.format("has reached the top"));
            }
            return;
        }

        int to = mCurrentPos + (int) deltaY;

        // over top
        if (to < 0) {
            if (DEBUG) {
                CLog.e(LOG_TAG, String.format("over top"));
            }
            to = 0;
        }

        mCurrentPos = to;
        updatePos();
        mLastPos = mCurrentPos;
    }

    private void updatePos() {
        int change = mCurrentPos - mLastPos;
        if (Version.hasHoneycomb()) {
            mHeaderContainer.offsetTopAndBottom(change);
            mContentViewContainer.offsetTopAndBottom(change);
        } else {
            RelativeLayout.LayoutParams lyp = (LayoutParams) mHeaderContainer.getLayoutParams();
            lyp.setMargins(0, mCurrentPos, 0, 0);
            ((LayoutParams) mContentViewContainer.getLayoutParams()).setMargins(0, mCurrentPos, 0, 0);
            requestLayout();
        }

        if (mCurrentPos < mOffsetToRotateView && mLastPos >= mOffsetToRotateView) {
            if (null != mPtrHandler) {
                mPtrHandler.crossRotateLineFromBottom(mIsInTouching);
            }
            if (mRotateView != null) {
                mRotateView.clearAnimation();
                if (mIsInTouching) {
                    mRotateView.startAnimation(mReverseFlipAnimation);
                }
            }
        } else if (mCurrentPos > mOffsetToRotateView && mLastPos <= mOffsetToRotateView) {
            if (null != mPtrHandler) {
                mPtrHandler.crossRotateLineFromTop(mIsInTouching);
            }
            if (mRotateView != null) {
                mRotateView.clearAnimation();
                if (mIsInTouching) {
                    mRotateView.startAnimation(mFlipAnimation);
                }
            }
        }
        mPtrHandler.onPercentageChange(mLastPos, mCurrentPos, mLastPos / mHeaderHeight, mCurrentPos / mHeaderHeight);
        onUpdatePos(mLastPos, mCurrentPos);
    }

    protected void onUpdatePos(int last, int now) {
    }

    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    private void release() {
        if (mPtrHandler != null) {
            mPtrHandler.onRelease();
        }
        if (mCurrentPos >= mOffsetToRefresh) {
            mState = State.PREPARE_REFRESH;
        }
        if (mKeepHeaderWhenRefresh && mCurrentPos >= mOffsetToRefresh) {
            mScrollChecker.scrollTo(mHeaderHeight, mDurationClose);
        } else {
            mScrollChecker.scrollTo(0, mDurationClose);
        }
    }

    private void notifyRefresh() {
        if (mPtrHandler != null && !mIsRefreshing) {
            mIsRefreshing = true;
            mPtrHandler.onRefresh();
        }
    }

    private void notifyBackToTop() {
        if (mPtrHandler != null) {
            mPtrHandler.onBackToTop();
        }
    }

    private void notifyScrollFinish() {
        if (mState == State.PREPARE_REFRESH) {
            mState = State.FREE;
            notifyRefresh();
        }
        if (mCurrentPos == 0) {
            notifyBackToTop();
        }
    }

    private boolean frameIsNotMoved() {
        return mCurrentPos == 0;
    }

    public void refreshComplete() {
        if (mIsRefreshing) {
            mIsRefreshing = false;
            if (mKeepHeaderWhenRefresh) {
                mScrollChecker.scrollTo(0, mDurationToCloseHeader);
            }
            if (mPtrHandler != null) {
                mPtrHandler.onRefreshComplete();
            }
        }
    }

    public void doRefresh() {
        int deltaY = mHeaderHeight - mCurrentPos;
        movePos(deltaY);
        notifyRefresh();
    }

    public void reset() {
        mIsRefreshing = false;
    }

    public void disableWhenHorizontalMove(boolean disable) {
        mDisableWhenHorizontalMove = disable;
    }

    public View getContent() {
        return mContentViewContainer;
    }

    public void setPtrHandler(PtrHandler ptrHandler) {
        mPtrHandler = ptrHandler;
    }

    public void setRotateView(int id) {
        mRotateView = findViewById(id);
    }

    public void setRotateView(View view) {
        mRotateView = view;
    }

    public void setResistance(float resistance) {
        mResistance = resistance;
    }

    public void setDurationClose(int duration) {
        mDurationClose = duration;
    }

    public void setDurationToCloseHeader(int duration) {
        mDurationToCloseHeader = duration;
    }

    public void setKeepHeaderWhenRefresh(boolean keepOrNot) {
        mKeepHeaderWhenRefresh = keepOrNot;
    }

    class ScrollChecker implements Runnable {

        private int mLastFlingY;
        private Scroller mScroller;
        private boolean mIsRunning = false;
        private int mStart;
        private int mTo;

        public ScrollChecker() {
            mScroller = new android.widget.Scroller(getContext());
        }

        public void run() {
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastFlingY;
            if (DEBUG) {
                // CLog.d(LOG_TAG, "scroll: %s, start: %s, to: %s, mCurrentPos: %s, current :%s, last: %s, delta: %s", finish, mStart, mTo, mCurrentPos, curY, mLastFlingY, deltaY);
            }
            if (!finish) {
                mLastFlingY = curY;
                movePos(deltaY);
                post(this);
            } else {
                finish();
            }
        }

        private void finish() {
            if (DEBUG) {
                // CLog.d(LOG_TAG, "finish, mCurrentPos:%s", mCurrentPos);
            }
            reset();
            notifyScrollFinish();
        }

        private void reset() {
            mIsRunning = false;
            mLastFlingY = 0;
            removeCallbacks(this);
        }

        public void abortIfWorking() {
            if (mIsRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                reset();
            }
        }

        public void scrollTo(int to, int duration) {
            mStart = mCurrentPos;
            mTo = to;
            int distance = to - mStart;
            if (DEBUG) {
                CLog.d(LOG_TAG, "scrollTo: start: %s, distance:%s, to:%s", mStart, distance, to);
            }
            if (mCurrentPos == 0) {
                return;
            }
            removeCallbacks(this);

            mLastFlingY = 0;
            mScroller = new android.widget.Scroller(getContext());
            mScroller.startScroll(0, 0, 0, distance, duration);
            post(this);
            mIsRunning = true;
        }
    }
}