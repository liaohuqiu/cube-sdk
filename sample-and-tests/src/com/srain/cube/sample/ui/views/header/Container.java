package com.srain.cube.sample.ui.views.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.srain.cube.sample.R;
import com.srain.cube.views.IScrollHideHeader;

public class Container extends LinearLayout implements IScrollHideHeader {

    public Container(Context context) {
        super(context);
    }

    public Container(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean reachTop() {
        return false;
    }
}