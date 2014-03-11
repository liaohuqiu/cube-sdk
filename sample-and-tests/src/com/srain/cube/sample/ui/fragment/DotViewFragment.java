package com.srain.cube.sample.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.srain.cube.sample.R;
import com.srain.cube.sample.activity.TitleBaseFragment;
import com.srain.cube.views.DotView;

public class DotViewFragment extends TitleBaseFragment {

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHeaderTitle("Dot View Demo");

		View view = inflater.inflate(R.layout.fragment_dotview, null);

		final DotView dotView = (DotView) view.findViewById(R.id.dot_view);
		dotView.setNum(10);

		((Button) view.findViewById(R.id.bt_next)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				int next = dotView.getCurrentIndex() + 1;
				if (next >= dotView.getTotal())
					next = 0;
				dotView.setSelected(next);
			}
		});
		return view;
	}
}