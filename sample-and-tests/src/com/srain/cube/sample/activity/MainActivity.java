package com.srain.cube.sample.activity;

import android.os.Bundle;

import com.srain.cube.app.XActivity;
import com.srain.cube.sample.R;
import com.srain.cube.sample.ui.fragment.HomeFragment;

public class MainActivity extends XActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_main);
        addFragment(HomeFragment.class, null);
    }

    protected String getCloseWarning() {
        return "Tap back to exit";
    }

    @Override
    protected int getFragmentContianerId() {
        return R.id.id_fragment;
    }
}