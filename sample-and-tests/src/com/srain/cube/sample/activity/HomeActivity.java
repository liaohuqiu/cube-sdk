package net.liaohuqiu.cube.sample.activity;

import android.os.Bundle;

import net.liaohuqiu.cube.app.XActivity;
import net.liaohuqiu.cube.sample.R;
import net.liaohuqiu.cube.sample.ui.fragment.HomeFragment;

public class HomeActivity extends XActivity {

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
    protected int getFragmentContainerId() {
        return R.id.id_fragment;
    }
}