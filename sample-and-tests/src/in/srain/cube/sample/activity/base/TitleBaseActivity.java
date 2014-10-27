package in.srain.cube.sample.activity.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import in.srain.cube.sample.R;

public abstract class TitleBaseActivity extends BaseActivity {

    protected TitleHeaderBar mTitleHeaderBar;
    protected LinearLayout mContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    protected void initViews() {
        super.setContentView(R.layout.activity_title_base);

        // 页头逻辑处理
        mTitleHeaderBar = (TitleHeaderBar) findViewById(R.id.ly_header_bar_title_wrap);
        mContainer = (LinearLayout) findViewById(R.id.ly_base_activity_container);

        if (enableDefaultBack()) {
            mTitleHeaderBar.setLeftOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {


                }
            });
        } else {
            mTitleHeaderBar.getLeftViewContainer().setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 重写，将内容置于LinearLayout中的统一的头部下方
     */
    @Override
    public void setContentView(int layoutResID) {
        View view = LayoutInflater.from(this).inflate(layoutResID, null);
        mContainer.addView(view);
    }

    /**
     * 是否使用默认的返回处理
     *
     * @return
     */
    protected boolean enableDefaultBack() {
        return true;
    }

    /**
     * 设置标题
     *
     * @param id
     */
    protected void setHeaderTitle(int id) {
        mTitleHeaderBar.getTitleTextView().setText(id);
    }

    /**
     * 设置标题
     */
    protected void setHeaderTitle(String title) {
        mTitleHeaderBar.setTitle(title);
    }
}
