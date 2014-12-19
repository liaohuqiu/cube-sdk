package in.srain.cube.request;

import android.os.Handler;
import android.os.Message;
import in.srain.cube.util.CLog;
import in.srain.cube.util.Debug;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author http://www.liaohuqiu.net
 */
public class SimpleRequestManager {

    private static final boolean DEBUG = Debug.DEBUG_REQUEST;
    private static final String LOG_TAG = Debug.DEBUG_REQUEST_LOG_TAG;

    private final static int REQUEST_SUCCESS = 0x01;

    private final static int REQUEST_FAILED = 0x02;

    public static <T> void sendRequest(final IRequest<T> request) {

        final Handler handler = new Handler() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REQUEST_SUCCESS:
                        request.onRequestSuccess((T) msg.obj);
                        break;

                    case REQUEST_FAILED:
                        request.onRequestFail(null);
                        break;

                    default:
                        break;
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                T data = null;
                try {

                    RequestData requestData = request.getRequestData();

                    if (DEBUG) {
                        CLog.d(LOG_TAG, "url: %s", requestData.getRequestUrl());
                    }
                    StringBuilder sb = new StringBuilder();
                    URL url = new URL(request.getRequestData().getRequestUrl());
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    if (requestData.shouldPost()) {
                        urlConnection.setRequestMethod("POST");
                        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                        writer.write(requestData.getPostString());
                        writer.flush();
                    }
                    InputStream ips = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(ips, "UTF-8"));

                    char[] buffer = new char[1024];
                    int bufferLength;
                    while ((bufferLength = reader.read(buffer, 0, buffer.length)) > 0) {
                        sb.append(buffer, 0, bufferLength);
                    }
                    reader.close();
                    ips.close();
                    data = request.onDataFromServer(sb.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (null == data) {
                    Message msg = Message.obtain();
                    msg.what = REQUEST_FAILED;
                    handler.sendMessage(msg);
                } else {
                    Message msg = Message.obtain();
                    msg.what = REQUEST_SUCCESS;
                    msg.obj = data;
                    handler.sendMessage(msg);
                }
            }
        }, "SimpleRequestBase-Manager").start();
    }
}
