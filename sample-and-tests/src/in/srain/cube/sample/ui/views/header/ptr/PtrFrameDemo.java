package in.srain.cube.sample.ui.views.header.ptr;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import in.srain.cube.sample.R;
import in.srain.cube.util.LocalDisplay;
import in.srain.cube.views.ptr.PtrFrame;

public class PtrFrameDemo extends PtrFrame {

    private TextView mTitleTextView;
    private Handler mHandler;

    public interface Handler {

        public void onRefresh();

        public boolean canDoRefresh();
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

    /**
     * customize the header view
     */
    private void initViews() {

        // special header
        mHeaderContainer = LayoutInflater.from(getContext()).inflate(R.layout.views_ptr_frame_header, null);
        mHeaderContainer.setLayoutParams(new LayoutParams(-1, LocalDisplay.dp2px(50)));
        addView(mHeaderContainer);

        // the rotate view
        setRotateView(R.id.ptr_id_image);

        mTitleTextView = (TextView) findViewById(R.id.ptr_header_title);

        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText("pull down");

        setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh() {
                return mHandler.canDoRefresh();
            }

            @Override
            public void onRefresh() {
                mHandler.onRefresh();
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
                mTitleTextView.setText("updating");
            }

            @Override
            public void crossRotateLineFromTop(boolean isInTouching) {
                if (isInTouching) {
                    mTitleTextView.setVisibility(VISIBLE);
                    mTitleTextView.setText("release");
                }
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
