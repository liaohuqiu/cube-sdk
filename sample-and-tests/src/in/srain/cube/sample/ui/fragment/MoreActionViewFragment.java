package in.srain.cube.sample.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.TitleBaseFragment;

public class MoreActionViewFragment extends TitleBaseFragment {

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHeaderTitle("More Action View Demo");

        mTitleHeaderBar.showMoreMenu();

        View view = inflater.inflate(R.layout.fragment_more_action_view, null);
        return view;
    }
}