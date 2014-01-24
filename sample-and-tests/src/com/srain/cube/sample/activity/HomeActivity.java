package com.srain.cube.sample.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.srain.cube.sample.R;
import com.srain.cube.util.LocalDisplay;

public class HomeActivity extends TitleBaseActivity {

	private RelativeLayout mViewContainer;
	private ArrayList<ItemInfo> mItemInfos = new ArrayList<HomeActivity.ItemInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		mViewContainer = (RelativeLayout) findViewById(R.id.ly_home_container);

		setHeaderTitle("Cube Demo");

		mItemInfos.add(new ItemInfo("Big Image List", "#4d90fe", new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("type", "big");
				intent.setClass(HomeActivity.this, ImageActivity.class);
				startActivity(intent);
			}
		}));

		mItemInfos.add(new ItemInfo("Grid Image List", "#b8ebf7", new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("type", "grid");
				intent.setClass(HomeActivity.this, ImageActivity.class);
				startActivity(intent);
			}
		}));

		mItemInfos.add(new ItemInfo("Small Image List", "#4d90fe", new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("type", "small");
				intent.setClass(HomeActivity.this, ImageActivity.class);
				startActivity(intent);
			}
		}));

		mItemInfos.add(new ItemInfo("Shop List", "#4d90fe", new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		}));

		setupList();
	}

	public void setupList() {

		int size = (LocalDisplay.SCREEN_WIDTH_PIXELS - LocalDisplay.dp2px(25 + 5 + 5)) / 3;
		int len = mItemInfos.size();

		int horizontalSpacing = LocalDisplay.dp2px(5);
		int verticalSpacing = LocalDisplay.dp2px(10.5f);

		for (int i = 0; i < len; i++) {

			RelativeLayout.LayoutParams lyp = new RelativeLayout.LayoutParams(size, size);
			int row = i / 3;
			int clo = i % 3;
			int left = 0;
			int top = 0;

			if (clo > 0) {
				left = (horizontalSpacing + size) * clo;
			}
			if (row > 0) {
				top = (verticalSpacing + size) * row;
			}
			lyp.setMargins(left, top, 0, 0);
			View view = getView(i, size);
			view.setOnClickListener(mItemInfos.get(i).mOnClickListener);
			view.setTag(i);
			mViewContainer.addView(view, lyp);
		}
	}

	protected View getView(int index, int size) {
		ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.item_home, null);
		view.setLayoutParams(new ViewGroup.LayoutParams(size, size));

		((TextView) view.findViewById(R.id.tv_item_home_title)).setText(mItemInfos.get(index).mTitle);
		view.findViewById(R.id.iv_item_home).setBackgroundColor(mItemInfos.get(index).getColor());
		return view;
	}

	@Override
	protected boolean enableDefaultBack() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	private static class ItemInfo {
		private String mColor;
		private String mTitle;
		private OnClickListener mOnClickListener;

		public ItemInfo(String title, String color, OnClickListener onClickListener) {
			mTitle = title;
			mColor = color;
			mOnClickListener = onClickListener;
		}

		private int getColor() {
			return Color.parseColor(mColor);
		}
	}
}
