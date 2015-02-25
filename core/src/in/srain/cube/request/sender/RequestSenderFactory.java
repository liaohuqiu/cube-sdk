package in.srain.cube.request.sender;

import in.srain.cube.request.FailData;
import in.srain.cube.request.IRequest;
import in.srain.cube.request.RequestData;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestSenderFactory {

    public static <T> BaseRequestSender create(IRequest<T> request) {
        BaseRequestSender sender = null;
        try {
            RequestData requestData = request.getRequestData();
            URL url = new URL(requestData.getRequestUrl());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (!requestData.shouldPost()) {
                sender = new GetRequestSender(request, urlConnection);
            } else {
                if (requestData.isMultiPart()) {
                    sender = new MultiPartRequestSender(request, urlConnection);
                } else {
                    sender = new PostRequestSender(request, urlConnection);
                }
            }

            sender.setup();

        } catch (MalformedURLException e) {
            request.setFailData(FailData.inputError(request));
        } catch (IOException e) {
            request.setFailData(FailData.networkError(request));
        }
        return sender;
    }
}
