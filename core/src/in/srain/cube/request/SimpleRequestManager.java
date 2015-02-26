package in.srain.cube.request;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import in.srain.cube.concurrent.SimpleTask;
import in.srain.cube.request.sender.BaseRequestSender;
import in.srain.cube.request.sender.RequestSenderFactory;
import in.srain.cube.util.CLog;
import in.srain.cube.util.CubeDebug;

/**
 * @author http://www.liaohuqiu.net
 */
public class SimpleRequestManager {

    private static final boolean DEBUG = CubeDebug.DEBUG_REQUEST;
    private static final String LOG_TAG = CubeDebug.DEBUG_REQUEST_LOG_TAG;

    private final static int REQUEST_SUCCESS = 0x01;

    private final static int REQUEST_FAILED = 0x02;

    public static <T> T requestSync(final IRequest<T> request) {
        T data = null;
        try {
            StringBuilder sb = new StringBuilder();
            RequestData requestData = request.getRequestData();
            if (DEBUG) {
                CLog.d(LOG_TAG, "%s", requestData);
            }
            BaseRequestSender requestSender = RequestSenderFactory.create(request);
            if (requestSender != null) {
                requestSender.send();
                requestSender.getResponse(sb);
                data = request.onDataFromServer(sb.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setFailData(FailData.networkError(request));
        }

        final T finalData = data;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (finalData == null) {
                    request.onRequestFail(request.getFailData());
                } else {
                    request.onRequestSuccess(finalData);
                }
            }
        };

        SimpleTask.post(runnable);

        return data;
    }

    public static <T> void sendRequest(final IRequest<T> request) {

        final Handler handler = new Handler(Looper.getMainLooper()) {
            @SuppressWarnings("unchecked")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REQUEST_SUCCESS:
                        request.onRequestSuccess((T) msg.obj);
                        break;

                    case REQUEST_FAILED:
                        request.onRequestFail(request.getFailData());
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
                    StringBuilder sb = new StringBuilder();
                    RequestData requestData = request.getRequestData();
                    if (DEBUG) {
                        CLog.d(LOG_TAG, "%s", requestData);
                    }
                    BaseRequestSender requestSender = RequestSenderFactory.create(request);
                    if (requestSender != null) {
                        requestSender.send();
                        requestSender.getResponse(sb);
                        data = request.onDataFromServer(sb.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    request.setFailData(FailData.networkError(request));
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
        }, "cube-simple-request-manager").start();
    }
}
