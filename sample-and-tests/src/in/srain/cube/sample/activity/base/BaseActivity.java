package in.srain.cube.sample.activity.base;

import in.srain.cube.app.XActivity;

public abstract class BaseActivity extends XActivity {

    @Override
    protected String getCloseWarning() {
        return "再按一次试试？";
    }

    @Override
    protected int getFragmentContainerId() {
        return 0;
    }
}