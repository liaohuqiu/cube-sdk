package com.srain.cube.sample.ui.views.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.srain.cube.sample.R;

/**
 * 普通标题头部的实现：
 * <ul>
 * <li>
 * 左侧返回
 * <li>
 * 中部标题
 * <li>
 * 右侧文字
 * </ul>
 * <p>
 * <a href="http://www.liaohuqiu.net/unified-title-header/">http://www.liaohuqiu.net/unified-title-header/</a>
 * 
 * @author http://www.liaohuqiu.net
 */
public class TitleHeaderBar extends HeaderBarBase {

	private TextView mTitleTextView;
	private TextView mRightTextView;
	private TextView mReturnImageView;
	private View mMoreAction;

	public TitleHeaderBar(Context context) {
		this(context, null);
	}

	public TitleHeaderBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TitleHeaderBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mReturnImageView = (TextView) findViewById(R.id.tv_title_bar_left);
		mTitleTextView = (TextView) findViewById(R.id.tv_title_bar_title);
		mRightTextView = (TextView) findViewById(R.id.tv_title_bar_right);
		mMoreAction = findViewById(R.id.ly_title_bar_more_action);
	}

	@Override
	protected int getLayoutId() {
		return R.layout.base_header_bar_title;
	}

	public TextView getLeftTextView() {
		return mReturnImageView;
	}

	public TextView getTitleTextView() {
		return mTitleTextView;
	}

	public TextView getRightTextView() {
		return mRightTextView;
	}

	public void showMoreMenu() {
		mRightTextView.setVisibility(GONE);
		mMoreAction.setVisibility(VISIBLE);
	}
}