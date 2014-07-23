package com.srain.cube.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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

	private GestureDetector mDetector;

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
		doLayout();
	}

	@SuppressWarnings("deprecation")
	protected void doLayout() {

		mDetector = new GestureDetector(getContext(), new MyOnGestureListener());
		mDetector.setIsLongpressEnabled(false);

		setDrawingCacheEnabled(false);
		setBackgroundDrawable(null);
		setClipChildren(false);

		ViewTreeObserver vto = mHeaderContainer.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new PTROnGlobalLayoutListener());
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
	 * if deltaY > 0, move the content up
	 */
	private void move(float deltaY) {

		// has reached the top
		if ((deltaY > 0 && mCurrentTop == -mHeaderHeight)) {
			if (DEBUG) {
				Log.d(LOG_TAG, String.format("has reached the top"));
			}
			return;
		}

		// has reached the bottom
		if ((deltaY < 0 && mCurrentTop == 0)) {
			if (DEBUG) {
				Log.d(LOG_TAG, String.format("has reached the bottom"));
			}
			return;
		}

		int to = mCurrentTop - (int) deltaY;

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
		moveTo(to);
	}

	private void moveTo(int to) {
		if (DEBUG) {
			Log.d(LOG_TAG, String.format("moveTo: %s %s, %s", to, mCurrentTop, mHeaderHeight));
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
		h = h - (mHeaderHeight + mCurrentTop);

		if (DEBUG) {
			Log.d(LOG_TAG, String.format("onMeasure %s", h));
		}
		mContentViewContainer.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		boolean handled = mDetector.onTouchEvent(e);
		if (handled) {
			return true;
		}
		return super.dispatchTouchEvent(e);
	}

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

			float speed = Math.abs(deltaY) / (cur.getEventTime() - curdown.getEventTime());
			boolean fastMove = speed > 0.8;

			// more readable code for develop
			boolean moveUp = deltaY > 0;
			boolean canMoveUp = mCurrentTop > -mHeaderHeight;
			boolean moveDown = !moveUp;
			boolean canMoveDown = mCurrentTop < 0;

			if (DEBUG) {
				// Log.d(LOG_TAG, String.format("moveUp: %s, canMoveUp: %s, moveDown: %s, canMoveDown: %s", moveUp, canMoveUp, moveDown, canMoveDown));
			}
			if (fastMove && moveDown && mCurrentTop < 0) {
				moveTo(0);
			} else {
				if ((moveUp && canMoveUp) || (moveDown && canMoveDown)) {
					move(deltaY);
				}
			}
			return false;
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
			int initialHeaderHeight = mHeaderContainer.getHeight();
			if (initialHeaderHeight > 0) {
				mHeaderHeight = initialHeaderHeight;
				mCurrentTop = 0;
				requestLayout();
			}
			getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}
	}
}