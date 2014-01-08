package com.srain.cube.sample.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.srain.cube.sample.R;

public class HeaderBar extends RelativeLayout {

	private TextView mTitleTextView;
	private TextView mLeftTextView;
	private View mLeftView;

	public HeaderBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.base_title_bar, this);

		mLeftView = findViewById(R.id.ly_title_bar_left);

		mLeftTextView = (TextView) findViewById(R.id.tv_title_bar_title_left);
		mTitleTextView = (TextView) findViewById(R.id.tv_title_bar_title);
	}

	public void setHeadTitle(int id) {
		mTitleTextView.setText(getContext().getString(id));
	}

	public void setHeadTitle(String title) {
		mTitleTextView.setText(title);
	}

	public void setLeftClickHandler(OnClickListener listener) {
		mLeftView.setOnClickListener(listener);
	}

	public void setLeftText(int id) {
		mLeftTextView.setText(getContext().getString(id));
	}

	public void setLeftText(String title) {
		mLeftTextView.setText(title);
	}

}