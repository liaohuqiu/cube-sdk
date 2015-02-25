package in.srain.cube.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import in.srain.cube.R;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;

/**
 * @author http://www.liaohuqiu.net
 */
public class ScrollHeaderFrame extends RelativeLayout {

    private static final boolean DEBUG = CubeDebug.DEBUG_SCROLL_HEADER_FRAME;
    private static final String LOG_TAG = ScrollHeaderFrame.class.getName();

    private int mHeaderHeight;
    private int mCurrentPos = 0;
    private int mLastPos = 0;
    private int mHeaderId = 0;
    private int mContainerId = 0;
    private boolean mDisabled = false;

    private ViewGroup mContentViewContainer;
    private View mHeaderContainer;
    private long mLastTime;

    private PointF mPtLastMove = new PointF();
    private IScrollHeaderFrameHandler mIScrollHeaderFrameHandler;

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
            if (arr.hasValue(R.styleable.ScrollHeaderFrame_scrollheaderframe_disable)) {
                mDisabled = arr.getBoolean(R.styleable.ScrollHeaderFrame_scrollheaderframe_disable, false);
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

    public void setHandler(IScrollHeaderFrameHandler handler) {
        mIScrollHeaderFrameHandler = handler;
        CLog.d(LOG_TAG, "setHandler: %s", this);
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
            Log.d(LOG_TAG, String.format("onLayout: current %s, %s %s %s %s", mCurrentPos, l, t, r, b));
        }

        int headerHeight = mHeaderContainer.getMeasuredHeight();
        if (headerHeight == 0) {
            return;
        }

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        int pos = 0;

        mHeaderContainer.layout(0, pos, w, pos + headerHeight);
        mContentViewContainer.layout(0, pos + headerHeight, w, h + headerHeight);
    }

    /**
     * if deltaY > 0, tryToMove the content down
     */
    private boolean tryToMove(float deltaY) {

        // has reached the bottom
        if ((deltaY > 0 && mCurrentPos == 0)) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("has reached the bottom"));
            }
            return false;
        }

        // has reached the top
        if ((deltaY < 0 && mCurrentPos == -mHeaderHeight)) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("has reached the top"));
            }
            return false;
        }

        int to = mCurrentPos + (int) deltaY;

        // over top
        if (to < -mHeaderHeight) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("over top"));
            }
            to = -mHeaderHeight;
        }

        // over bottom
        if (to > 0) {
            if (DEBUG) {
                Log.d(LOG_TAG, String.format("over bottom"));
            }
            to = 0;
        }
        return moveTo(to);
    }

    private boolean moveTo(int to) {
        if (DEBUG) {
            Log.d(LOG_TAG, String.format("moveTo: %s %s, %s", to, mCurrentPos, mHeaderHeight));
        }
        if (mCurrentPos == to) {
            return false;
        }
        int y = mCurrentPos - to;
        mCurrentPos = to;
        updatePos();
        return true;
    }

    private void updatePos() {
        int change = mCurrentPos - mLastPos;

        // mHeaderContainer.scrollTo(0, -change);
        // mContentViewContainer.scrollTo(0, -change);
        scrollTo(0, -change);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        mHeaderHeight = mHeaderContainer.getMeasuredHeight();

        if (DEBUG) {
            Log.d(LOG_TAG, String.format("onMeasure %s getMeasuredHeight: %s", h, mHeaderContainer.getMeasuredHeight()));
        }

        mContentViewContainer.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        if (!isEnabled() || mDisabled) {
            return super.dispatchTouchEvent(e);
        }
        boolean handled = super.dispatchTouchEvent(e);
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_DOWN:
                mLastTime = e.getEventTime();
                mPtLastMove.set(e.getX(), e.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                if (mHeaderHeight == 0) {
                    break;
                }

                float deltaY = (int) (e.getY() - mPtLastMove.y);
                mPtLastMove.set(e.getX(), e.getY());

                float speed = Math.abs(deltaY / (mLastTime - e.getEventTime()));
                mLastTime = e.getEventTime();

                boolean moveUp = deltaY < 0;
                boolean canMoveUp = mCurrentPos > -mHeaderHeight;
                boolean moveDown = !moveUp;
                boolean canMoveDown = mCurrentPos < 0;
                if (DEBUG) {
                    Log.d(LOG_TAG, String.format("ACTION_MOVE: %s, speed: %s, moveUp: %s, canMoveUp: %s, moveDown: %s, canMoveDown: %s", speed, deltaY, moveUp, canMoveUp, moveDown, canMoveDown));
                }

                // disable move when header not reach top
                if (moveDown && mIScrollHeaderFrameHandler != null && !mIScrollHeaderFrameHandler.hasReachTop()) {
                    return handled;
                }

                if (speed >= 5 && moveDown && mIScrollHeaderFrameHandler != null) {
                    moveTo(0);
                    return handled;
                }
                if (speed >= 5 && moveUp && mIScrollHeaderFrameHandler != null) {
                    moveTo(-mHeaderHeight);
                    return handled;
                }

                if ((moveUp && canMoveUp) || (moveDown && canMoveDown)) {
                    tryToMove(deltaY);
                }
                break;
            default:
                break;
        }
        return handled;
    }
}