package in.srain.cube.views.loadmore;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

/**
 * custom loadmore ScrollView
 * 
 * content must be full or out of screen size : Y 
 * 
 * @author qs
 * @email qs_lll@163.com
 * @phone 15618965173
 */
public class CubeScrollView extends ScrollView {
	OnScrollChangedListener onScroller;

	public CubeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public CubeScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CubeScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		// TODO Auto-generated method stub
		// Log.e("change ", l + " " + t + " " + getScrollRange());
		if (onScroller != null) {
			onScroller.onScrollChange(t, getScrollRange());
		}
		super.onScrollChanged(l, t, oldl, oldt);

	}

	private int getScrollRange() {
		int scrollRange = 0;
		if (getChildCount() > 0) {
			View child = getChildAt(0);
			scrollRange = Math.max(0, child.getHeight()
					- (getHeight() - getPaddingBottom() - getPaddingTop()));
		}
		return scrollRange;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:

			if (onScroller != null) {
				onScroller.onActionUp();
			}

			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	public void setOnScrollChangedListener(OnScrollChangedListener onScroller) {
		this.onScroller = onScroller;
	}

	public interface OnScrollChangedListener {
		public abstract void onScrollChange(int Y, int Yrange);

		public abstract void onActionUp();

	}

}