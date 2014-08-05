package net.liaohuqiu.cube.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

/**
 * 支持ListView置顶功能
 */
public class KBPinnedHeaderListView extends ListView {

	// ===========================================================
	// Interfaces & enumeration
	// ===========================================================

	public interface PinnedHeaderListViewController {
		public PinnedHeaderStatus getPinnedHeaderState(int position);

		public void showPinnedHeader(View headerView, int position);

		public View createHeader();
	}

	public static enum PinnedHeaderStatus {
		Gone, Visible, PushedUp
	}

	// ===========================================================
	// Fields
	// ===========================================================

	private View mHeaderView;
	private int mMeasuredWidth;
	private int mMeasuredHeight;
	private boolean mDrawFlag = true;

	// ===========================================================
	// Constructors
	// ===========================================================

	public KBPinnedHeaderListView(Context context) {
		super(context);
	}

	public KBPinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public KBPinnedHeaderListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	/**
	 * 设置置顶的Header View
	 * 
	 * @param pHeader
	 */
	public void setPinnedHeader(View pHeader) {
		mHeaderView = pHeader;
		requestLayout();
	}

	public View getPinnedHander() {
		return mHeaderView;
	}

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// 三个覆写方法负责在当前窗口显示inflate创建的Header View

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (null != mHeaderView && mHeaderView.getVisibility() == View.VISIBLE) {
			measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
			mMeasuredWidth = mHeaderView.getMeasuredWidth();
			mMeasuredHeight = mHeaderView.getMeasuredHeight();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		if (null != mHeaderView && mHeaderView.getVisibility() == View.VISIBLE) {
			mHeaderView.layout(0, 0, mMeasuredWidth, mMeasuredHeight);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (null != mHeaderView && mHeaderView.getVisibility() == View.VISIBLE && mDrawFlag) {
			drawChild(canvas, mHeaderView, getDrawingTime());
		}
	}

	// ===========================================================
	// Methods
	// ===========================================================
	/**
	 * HeaderView三种状态的具体处理
	 * 
	 * @param position
	 */
	public void controlPinnedHeader(PinnedHeaderStatus pinnedHeaderState) {
		if (null == mHeaderView || mHeaderView.getVisibility() != View.VISIBLE) {
			return;
		}

		if (pinnedHeaderState == PinnedHeaderStatus.Gone) {
			mDrawFlag = false;
		} else if (pinnedHeaderState == PinnedHeaderStatus.Visible) {
			mDrawFlag = true;
			mHeaderView.layout(0, 0, mMeasuredWidth, mMeasuredHeight);
		} else {

			mDrawFlag = true;
			// 移动位置
			View topItem = getChildAt(0);

			if (null != topItem) {
				int bottom = topItem.getBottom();
				int height = mHeaderView.getHeight();

				int y;
				if (bottom < height) {
					y = bottom - height;
				} else {
					y = 0;
				}

				if (mHeaderView.getTop() != y) {
					mHeaderView.layout(0, y, mMeasuredWidth, mMeasuredHeight + y);
				}

			}
		}
	}
}
