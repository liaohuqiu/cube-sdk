package com.srain.cube.sample.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.srain.cube.app.XActivity;
import com.srain.cube.sample.R;
import com.srain.cube.sample.ui.views.header.TitleHeaderBar;

/**
 * 带页头的 Activity基类
 * 
 * <p>
 * 使用一个orientation="vertical", LinearLayout，包含一个统一的页头{@link TitleHeaderBar} , 内容置于页头下部
 * 
 * <p>
 * <a href="http://www.liaohuqiu.net/unified-title-header/">http://www.liaohuqiu.net/unified-title-header/</a>
 * 
 * @author huqiu.lhq
 */
public abstract class TitleBaseActivity extends XActivity {

	protected TitleHeaderBar mTitleHeaderBar;
	private LinearLayout mContentViewContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View view = LayoutInflater.from(this).inflate(R.layout.activity_title_base, null);
		mContentViewContainer = (LinearLayout) view.findViewById(R.id.ly_main_content_container);
		super.setContentView(view);

		// 页头逻辑处理
		mTitleHeaderBar = (TitleHeaderBar) findViewById(R.id.ly_header_bar_title_wrap);
		if (enableDefaultBack()) {
			mTitleHeaderBar.getLeftTextView().setText(R.string.base_title_return);
			mTitleHeaderBar.setLeftOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					onBackPressed();
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
		checkCall();
		View view = LayoutInflater.from(this).inflate(layoutResID, null);
		mContentViewContainer.addView(view);
	}

	@Override
	public void setContentView(View view) {
		checkCall();
		mContentViewContainer.addView(view);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		checkCall();
		mContentViewContainer.addView(view, params);
	}

	private void checkCall() {
		if (null == mContentViewContainer) {
			throw new RuntimeException("You should call 'super.onCreate(savedInstanceState)' first before setContentView()");
		}
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
	 * 
	 * @param id
	 */
	protected void setHeaderTitle(String title) {
		mTitleHeaderBar.getTitleTextView().setText(title);
	}

	public TitleHeaderBar getTitleHeaderBar() {
		return mTitleHeaderBar;
	}
}
