package in.srain.cube.sample.ui.views.header.ptr;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import in.srain.cube.sample.R;
import in.srain.cube.sample.ui.views.StoreHouseHeader;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.ptr.PtrFrame;

public class PtrFrameDemo extends PtrFrame {

    private TextView mTitleTextView;
    private Handler mHandler;

    private SwipeProgressBar mProgressBar;

    public static abstract class DefaultHandler implements Handler {

        @Override
        public boolean canDoRefresh(PtrFrameDemo frame) {
            return frame.canChildScrollUp();
        }
    }

    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mContentViewContainer instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mContentViewContainer;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mContentViewContainer.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mContentViewContainer, -1);
        }
    }

    public interface Handler {

        public void onRefresh();

        public boolean canDoRefresh(PtrFrameDemo frame);
    }

    public PtrFrameDemo(Context context) {
        super(context);
    }

    public PtrFrameDemo(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public PtrFrameDemo(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mProgressBar.draw(canvas);
    }

    private void setColorScheme(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        final Resources res = getResources();
        final int color1 = res.getColor(colorRes1);
        final int color2 = res.getColor(colorRes2);
        final int color3 = res.getColor(colorRes3);
        final int color4 = res.getColor(colorRes4);
        mProgressBar.setColorScheme(color1, color2, color3, color4);
    }

    /**
     * customize the header view
     */
    private void initViews() {
        setWillNotDraw(false);

        // special header
        RelativeLayout container = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.views_ptr_frame_header, null);
        mHeaderContainer = container;
        mHeaderContainer.setLayoutParams(new LayoutParams(-1, LocalDisplay.dp2px(50)));
        addView(mHeaderContainer);

        mProgressBar = new SwipeProgressBar(this);
        mProgressBar.setBounds(0, 0, LocalDisplay.SCREEN_WIDTH_PIXELS, LocalDisplay.dp2px(50));
        setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        // the rotate view
        setRotateView(R.id.ptr_id_image);

        mTitleTextView = (TextView) findViewById(R.id.ptr_header_title);

        mTitleTextView.setVisibility(VISIBLE);
        // mTitleTextView.setText("pull down");

        setPtrHandler(new PtrHandler() {

            /**
             * if content is empty or the first child is in view, should do refresh
             * after release
             *
             * @param frame
             * @param content
             * @param header
             */
            @Override
            public boolean checkCanDoRefresh(PtrFrame frame, View content, View header) {
                return mHandler.canDoRefresh(PtrFrameDemo.this);
            }

            @Override
            public void onRefresh() {
                mHandler.onRefresh();
                mRotateView.setVisibility(INVISIBLE);
                mTitleTextView.setVisibility(VISIBLE);
                mTitleTextView.setText("updating...");
                mProgressBar.start();
            }

            @Override
            public void onBackToTop() {
                mRotateView.setVisibility(VISIBLE);
                mTitleTextView.setVisibility(VISIBLE);
                mTitleTextView.setText("pull down");
            }

            @Override
            public void onRelease() {
                mRotateView.setVisibility(INVISIBLE);
                mTitleTextView.setVisibility(VISIBLE);
                mTitleTextView.setText("updating...");
            }

            @Override
            public void onRefreshComplete() {
                mProgressBar.stop();
            }

            @Override
            public void crossRotateLineFromTop(boolean isInTouching) {
                if (isInTouching) {
                    mTitleTextView.setVisibility(VISIBLE);
                    mTitleTextView.setText("release");
                }
            }

            @Override
            public void onPercentageChange(int oldPosition, int newPosition, float oldPercent, float newPercent) {
                float f = newPosition * 1f / getHeaderHeight();
                if (f > 1) f = 1;
                mProgressBar.setTriggerPercentage(f);
                invalidate();
            }

            @Override
            public void crossRotateLineFromBottom(boolean isInTouching) {
                if (isInTouching) {
                    mTitleTextView.setVisibility(VISIBLE);
                    mTitleTextView.setText("pull down");
                }
            }
        });
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }
}
