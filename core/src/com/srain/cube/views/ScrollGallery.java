package com.srain.cube.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * A Gallery which can scroll to left or right.
 * 
 * @author huqiu.lhq
 * 
 * @deprecated As a extend of Gallery, it is deprecated.
 */
@Deprecated
public class ScrollGallery extends Gallery {

	public ScrollGallery(Context context) {
		super(context);
	}

	public ScrollGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	public boolean playNext() {
		int current = getSelectedItemPosition();
		int total = computeHorizontalScrollRange();
		if (total > 0 && current < total - 1) {
			onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null);
			return true;
		}
		return false;
	}

	public boolean playPrevious() {
		int current = getSelectedItemPosition();
		int total = computeHorizontalScrollRange();
		if (total > 0 && current > 0) {
			onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
			return true;
		}
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (isScrollingLeft(e1, e2)) {
			playPrevious();
		} else {
			playNext();
		}
		return true;
	}
}
