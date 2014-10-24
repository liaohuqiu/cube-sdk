package in.srain.cube.views.banner;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class BannerAdapter extends PagerAdapter {

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getView(LayoutInflater.from(container.getContext()), position);
        container.addView(view);
        return view;
    }

    public int getPositionForIndicator(int position) {
        return position;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public abstract View getView(LayoutInflater layoutInflater, int position);

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == o;
    }
}
