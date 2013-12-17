package com.srain.sdk.app;

import com.srain.sdk.app.FragmentParameter.TYPE;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public abstract class BaseFragmentActivity extends FragmentActivity {

	/**
	 * return the string id of close warning
	 * 
	 * return value which lower than 1 will exit instantly when press back key
	 * 
	 * @return
	 */
	protected abstract int getCloseWarning();

	protected BaseFragment currentFragment;
	private boolean mCloseWarned;

	public void pushFragmentToBackStatck(Class<?> cls, int containerId, Object data) {
		FragmentParameter param = new FragmentParameter();
		param.cls = cls;
		param.containerId = containerId;
		param.data = data;
		param.addToBackStack = true;

		processFragement(param);
	}

	public void addFragment(Class<?> cls, int containerId, Object data) {

		FragmentParameter param = new FragmentParameter();
		param.cls = cls;
		param.containerId = containerId;
		param.data = data;
		param.addToBackStack = false;

		processFragement(param);
	}

	public void replaceFragment(Class<?> cls, int containerId, Object data) {

		FragmentParameter param = new FragmentParameter();
		param.cls = cls;
		param.containerId = containerId;
		param.data = data;
		param.type = TYPE.REPLACE;
		param.addToBackStack = false;

		processFragement(param);
	}

	protected String getFragmentTag(Class<?> cls) {
		return cls.toString();
	}

	private void processFragement(FragmentParameter param) {

		Class<?> cls = param.cls;
		if (cls == null) {
			return;
		}
		try {
			String fragmentTag = getFragmentTag(cls);
			BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
			if (fragment == null) {
				fragment = (BaseFragment) cls.newInstance();
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
					ft.add(param.containerId, fragment, fragmentTag);
				}
			} else {
				ft.replace(param.containerId, fragment, fragmentTag);
			}

			currentFragment = fragment;
			if (param.addToBackStack)
				ft.addToBackStack(fragmentTag);
			ft.setTransition(param.transition);
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
		BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(cls.toString());
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
		currentFragment = (BaseFragment) fm.findFragmentByTag(name);
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
	public void onBackPressed() {
		boolean enableBackPressed = true;
		if (currentFragment != null) {
			enableBackPressed = !currentFragment.stayWhenBackPressed();
		}
		if (enableBackPressed) {
			int cnt = getSupportFragmentManager().getBackStackEntryCount();
			if (cnt <= 0) {
				int closeWarningHint = getCloseWarning();
				if (!mCloseWarned && closeWarningHint > 0) {
					showError(getString(closeWarningHint));
					mCloseWarned = true;
				} else {
					super.onBackPressed();
				}
			} else {
				mCloseWarned = false;
				super.onBackPressed();
			}
		}
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

	public void showError(String word) {
		Toast toast = Toast.makeText(this, word, Toast.LENGTH_SHORT);
		toast.show();
	}

	protected void exitFullScreen() {
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	}
}
