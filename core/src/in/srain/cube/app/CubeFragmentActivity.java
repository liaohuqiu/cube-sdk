package in.srain.cube.app;

import android.app.ActivityManager;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import in.srain.cube.app.FragmentParam.TYPE;
import in.srain.cube.util.CLog;

import java.util.List;

public abstract class CubeFragmentActivity extends FragmentActivity {

    private final static String TAG_SP = "_";

    /**
     * return the string id of close warning
     * <p/>
     * return value which lower than 1 will exit instantly when press back key
     *
     * @return
     */
    protected abstract String getCloseWarning();

    protected abstract int getFragmentContainerId();

    protected CubeFragment currentFragment;
    private boolean mCloseWarned;

    public void pushFragmentToBackStack(Class<?> cls, Object data) {
        FragmentParam param = new FragmentParam();
        param.cls = cls;
        param.data = data;
        param.addToBackStack = true;

        processFragment(param);
    }

    public void addFragment(Class<?> cls, Object data) {

        FragmentParam param = new FragmentParam();
        param.cls = cls;
        param.data = data;
        param.addToBackStack = false;
        processFragment(param);
    }

    public void replaceFragment(Class<?> cls, Object data) {

        FragmentParam param = new FragmentParam();
        param.cls = cls;
        param.data = data;
        param.type = TYPE.REPLACE;
        param.addToBackStack = false;

        processFragment(param);
    }

    protected String getFragmentTag(FragmentParam param) {

        StringBuilder sb = new StringBuilder(param.cls.toString());
        return sb.toString();
    }

    private void processFragment(FragmentParam param) {
        int containerId = getFragmentContainerId();
        Class<?> cls = param.cls;
        if (cls == null) {
            return;
        }
        try {
            String fragmentTag = getFragmentTag(param);
            CubeFragment fragment = (CubeFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (fragment == null) {
                fragment = (CubeFragment) cls.newInstance();
            }
            fragment.onComeIn(param.data);
            if (currentFragment != null) {
                currentFragment.onLeave();
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            if (param.type == TYPE.ADD) {
                if (fragment.isAdded()) {
                    ft.show(fragment);
                } else {
                    ft.add(containerId, fragment, fragmentTag);
                }
            } else {
                ft.replace(containerId, fragment, fragmentTag);
            }

            currentFragment = fragment;
            if (param.addToBackStack) {
                ft.addToBackStack(fragmentTag);
            }
            ft.commitAllowingStateLoss();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        mCloseWarned = false;
    }

    public void goToFragment(Class<?> cls, Object data) {
        if (cls == null) {
            return;
        }
        CubeFragment fragment = (CubeFragment) getSupportFragmentManager().findFragmentByTag(cls.toString());
        if (fragment != null) {
            currentFragment = fragment;
            fragment.onBack(data);
        }
        getSupportFragmentManager().popBackStackImmediate(cls.toString(), 0);
    }

    public void popTopFragment(Object data) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStackImmediate();
        currentFragment = null;
        int cnt = fm.getBackStackEntryCount();
        String name = fm.getBackStackEntryAt(cnt - 1).getName();
        currentFragment = (CubeFragment) fm.findFragmentByTag(name);
        currentFragment.onBack(data);
    }

    public void popToRoot(Object data) {
        FragmentManager fm = getSupportFragmentManager();
        while (fm.getBackStackEntryCount() > 1) {
            fm.popBackStackImmediate();
        }
        popTopFragment(data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        debugTaskInfo();
    }

    protected void debugTaskInfo() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = manager.getRunningTasks(10);
        CLog.d("test", "debugTaskInfo: %s %s", this.getClass().getName(), taskList.size());

        for (int i = 0; i < taskList.size(); i++) {
            ActivityManager.RunningTaskInfo info = taskList.get(i);
            CLog.d("test", "RunningTaskInfo: %d %s, %s", info.numActivities, info.topActivity.getClassName(), info.baseActivity.getClassName());
        }
    }

    @Override
    public void onBackPressed() {
        boolean enableBackPressed = true;
        if (currentFragment != null) {
            enableBackPressed = !currentFragment.stayWhenBackPressed();
        }
        if (enableBackPressed) {
            int cnt = getSupportFragmentManager().getBackStackEntryCount();
            if (cnt <= 0 && isTaskRoot()) {
                String closeWarningHint = getCloseWarning();
                if (!mCloseWarned && !TextUtils.isEmpty(closeWarningHint)) {
                    Toast toast = Toast.makeText(this, closeWarningHint, Toast.LENGTH_SHORT);
                    toast.show();
                    mCloseWarned = true;
                } else {
                    returnBack();
                }
            } else {
                mCloseWarned = false;
                returnBack();
            }
        }
    }

    protected void returnBack() {
        super.onBackPressed();
    }

    public void hideKeyboardForCurrentFocus() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void showKeyboardAtView(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    protected void exitFullScreen() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }
}
