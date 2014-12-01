package in.srain.cube.mints.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import in.srain.cube.R;

public abstract class TitleBaseActivity extends DemoBaseActivity {

    protected TitleHeaderBar mTitleHeaderBar;
    protected LinearLayout mContentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    protected int getLayoutId() {
        return R.layout.cube_mints_base_content_frame_with_title_header;
    }

    protected void initViews() {
        super.setContentView(getLayoutId());

        // 页头逻辑处理
        mTitleHeaderBar = (TitleHeaderBar) findViewById(R.id.cube_mints_content_frame_title_header);
        mContentContainer = (LinearLayout) findViewById(R.id.cube_mints_content_frame_content);

        if (enableDefaultBack()) {
            mTitleHeaderBar.setLeftOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!processBackPressed()) {
                        doReturnBack();
                    }
                }
            });
        } else {
            mTitleHeaderBar.getLeftViewContainer().setVisibility(View.INVISIBLE);
        }
    }

    protected boolean enableDefaultBack() {
        return true;
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(this).inflate(layoutResID, null);
        mContentContainer.addView(view);
    }


    protected void setHeaderTitle(int id) {
        mTitleHeaderBar.getTitleTextView().setText(id);
    }

    protected void setHeaderTitle(String title) {
        mTitleHeaderBar.setTitle(title);
    }
}
