package in.srain.cube.request.sender;

import in.srain.cube.request.IRequest;
import in.srain.cube.request.RequestData;

import java.io.IOException;
import java.net.HttpURLConnection;

public class GetRequestSender extends BaseRequestSender {

    public GetRequestSender(IRequest<?> request, HttpURLConnection httpURLConnection, RequestData requestData) {
        super(request, httpURLConnection, requestData);
    }

    @Override
    public void send() {

    }
}
