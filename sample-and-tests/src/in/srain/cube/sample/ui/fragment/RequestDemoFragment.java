package in.srain.cube.sample.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import in.srain.cube.request.JsonData;
import in.srain.cube.request.JsonRequestSuccHandler;
import in.srain.cube.sample.R;
import in.srain.cube.sample.activity.TitleBaseFragment;
import in.srain.cube.sample.data.SampleRequest;

public class RequestDemoFragment extends TitleBaseFragment {

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHeaderTitle("Request Demo");
		View view = inflater.inflate(R.layout.fragment_request, null);
		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		final EditText inpuText = (EditText) view.findViewById(R.id.input_request_demo_str);
		final TextView button = (TextView) view.findViewById(R.id.btn_request_demo_time);
		final TextView okTextView = (TextView) view.findViewById(R.id.tv_request_demo_ok);
		final TextView resultTimeTextView = (TextView) view.findViewById(R.id.tv_request_demo_result);
		final TextView serverTimeTextView = (TextView) view.findViewById(R.id.tv_request_demo_server_time);

		OnClickListener onClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {

				button.setText("Requesting...");

				String str = inpuText.getText().toString();
				SampleRequest.reverse(str, new JsonRequestSuccHandler() {

					@Override
					public void onRequestFinish(JsonData jsonData) {

						button.setText("Click to request");
						okTextView.setText(jsonData.optString("ok"));
						resultTimeTextView.setText(jsonData.optString("result"));
						serverTimeTextView.setText(jsonData.optString("server_time"));
					}
				});

			}
		};

		button.setText("Click to request");
		button.setOnClickListener(onClickListener);
		return view;
	}
}