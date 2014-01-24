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
import android.widget.RelativeLayout;

import com.srain.cube.R;

/**
 * 
 * @author huqiu.lhq
 */
public class ScrollHeaderFrame extends RelativeLayout {

	private static final boolean DEBUG = false;
	private static final String LOG_TAG = ScrollHeaderFrame.class.getName();

	private int mHeaderHeight;
	private int mLastTop = 0;
	private int mHeaderId = 0;
	private int mContainerId = 0;
	private int mFooterId = 0;

	private ViewGroup mContentViewContainer;
	private View mHeaderContainer;
	private View mFooterContainer;

	private GestureDetector mDetector;
	private Boolean mContentIsInVisibale = true;

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

			if (arr.hasValue(R.styleable.ScrollHeaderFrame_scrollheaderframe_footer)) {
				mFooterId = arr.getResourceId(R.styleable.ScrollHeaderFrame_scrollheaderframe_footer, 0);
			}
			arr.recycle();
		}
	}

	public View getContentView() {
		return mContentViewContainer;
	}

	public View getFooterView() {
		return mFooterContainer;
	}

	public View getHeaderView() {
		return mHeaderContainer;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mHeaderContainer = findViewById(mHeaderId);
		mFooterContainer = findViewById(mFooterId);
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

		int headerHeight = mHeaderContainer.getMeasuredHeight();
		int footerHeight = 0;
		if (mFooterContainer != null) {
			footerHeight = mFooterContainer.getMeasuredHeight();
		}
		if (headerHeight == 0) {
			return;
		}

		int w = getMeasuredWidth();
		int h = getMeasuredHeight();

		int headerOffset = mLastTop;

		mHeaderContainer.layout(0, headerOffset, w, headerHeight + headerOffset);

		if (mContentViewContainer.getVisibility() == VISIBLE) {
			mContentIsInVisibale = true;
			if (mFooterContainer != null && mFooterContainer.getVisibility() == VISIBLE) {
				mContentViewContainer.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h - footerHeight, MeasureSpec.EXACTLY));
				mContentViewContainer.layout(0, headerHeight + headerOffset, w, h - footerHeight);
				mFooterContainer.layout(0, h - footerHeight, w, h);
			} else {
				mContentViewContainer.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
				mContentViewContainer.layout(0, headerHeight + headerOffset, w, h);
				if (null != mFooterContainer) {
					mFooterContainer.layout(0, 0, 0, 0);
				}
			}
		} else {
			mContentIsInVisibale = false;
			mContentViewContainer.layout(0, 0, 0, 0);

			if (mFooterContainer != null) {
				if (mFooterContainer.getVisibility() == VISIBLE) {
					mFooterContainer.layout(0, headerHeight + headerOffset, w, headerHeight + headerOffset + footerHeight);
				} else {
					mFooterContainer.layout(0, 0, 0, 0);
				}
			}
		}
	}

	/**
	 * if deltaY > 0, move the content up
	 */
	private boolean move(float deltaY) {

		int top = mHeaderContainer.getTop();
		// has reached the top
		if ((deltaY > 0 && top == -mHeaderHeight)) {
			return false;
		}

		// has reached the bottom
		if ((deltaY < 0 && top == 0)) {
			return false;
		}

		moveUp(deltaY);

		// adjust
		top = mHeaderContainer.getTop();
		if (top < -mHeaderHeight) {
			deltaY = top - (-mHeaderHeight);
			moveUp(deltaY);
		}

		top = mHeaderContainer.getTop();
		if (top > 0) {
			deltaY = top - 0;
			moveUp(deltaY);
		}
		invalidate();

		top = mHeaderContainer.getTop();
		mLastTop = top;
		return false;
	}

	private void moveUp(float deltaY) {
		mHeaderContainer.offsetTopAndBottom((int) -deltaY);
		mContentViewContainer.offsetTopAndBottom((int) -deltaY);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		if (mContentIsInVisibale) {
			mDetector.onTouchEvent(e);
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
			if (!mContentIsInVisibale) {
				return false;
			}

			float speed = Math.abs(deltaY) / (cur.getEventTime() - curdown.getEventTime());
			boolean fastMove = speed > 0.8;

			int top = mHeaderContainer.getTop();

			// more readable code for develop
			boolean moveUp = deltaY > 0;
			boolean canMoveUp = top > -mHeaderHeight;
			boolean moveDown = !moveUp;
			boolean canMoveDown = top < 0;

			if (DEBUG) {
				Log.d(LOG_TAG, String.format("moveUp: %s, canMoveUp: %s, moveDown: %s, canMoveDown: %s", moveUp, canMoveUp, moveDown, canMoveDown));
			}
			if (fastMove && moveDown && top < 0) {
				move(top);
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
		@Override
		public void onGlobalLayout() {
			int initialHeaderHeight = mHeaderContainer.getHeight();
			if (initialHeaderHeight > 0) {
				mHeaderHeight = initialHeaderHeight;
				mLastTop = 0;
				requestLayout();
			}
			getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}
	}
}