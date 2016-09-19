package in.srain.cube.mints.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import java.util.ArrayList;

public abstract class MenuItemFragment extends TitleBaseFragment {

    protected ArrayList<MenuItemInfo> mItemInfos = new ArrayList<MenuItemInfo>();

    protected abstract void addItemInfo(ArrayList<MenuItemInfo> itemInfos);

    protected abstract void setupViews(View view);

    protected MenuItemInfo newItemInfo(String title, int color, OnClickListener onClickListener) {
        return new MenuItemInfo(title, getResources().getColor(color), onClickListener);
    }

    protected MenuItemInfo newItemInfo(String title, String color, OnClickListener onClickListener) {
        return new MenuItemInfo(title, Color.parseColor(color), onClickListener);
    }

    protected MenuItemInfo newItemInfo(int title, String color, OnClickListener onClickListener) {
        return new MenuItemInfo(getString(title), Color.parseColor(color), onClickListener);
    }

    protected MenuItemInfo newItemInfo(int title, int color, OnClickListener onClickListener) {
        return new MenuItemInfo(getString(title), getResources().getColor(color), onClickListener);
    }

    protected abstract int getLayoutId();

    @Override
    protected View createView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), null);
        addItemInfo(mItemInfos);
        setupViews(view);
        return view;
    }

    @Override
    protected boolean enableDefaultBack() {
        return false;
    }

    protected static class MenuItemInfo {
        private int mColor;
        private String mTitle;
        private String mDes;
        private OnClickListener mOnClickListener;

        public MenuItemInfo(String title, int color, OnClickListener onClickListener) {
            mTitle = title;
            mColor = color;
            mOnClickListener = onClickListener;
        }

        public void onClick(View v) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(v);
            }
        }

        public int getColor() {
            return mColor;
        }

        public String getTitle() {
            return mTitle;
        }
    }
}
