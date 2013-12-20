package com.srain.cube.request;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.srain.cube.Cube;

import android.os.Handler;
import android.os.Message;

/**
 * Perform the network call and call the callback interface after request is done.
 * 
 * @author huqiu.lhq
 */
public class RequestManager {

	private final static int REQUEST_SUCC = 0x01;

	public static <T> void sendRequest(final IRequest<T> request) {

		final Handler handler = new Handler() {
			@SuppressWarnings("unchecked")
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REQUEST_SUCC:
					request.onRequestSucc((T) msg.obj);
					break;

				default:
					break;
				}
			}
		};

		Cube.getInstance().getAsyncHttpClient().get(request.getRequestUrl(), new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, final String content) {
				super.onSuccess(statusCode, content);
				new Thread(new Runnable() {
					@Override
					public void run() {
						JsonData jsonData = JsonData.create(content);
						Message msg = Message.obtain();
						msg.what = REQUEST_SUCC;
						msg.obj = jsonData;
						handler.sendMessage(msg);
					}
				}).start();
			}
		});
	}
}
