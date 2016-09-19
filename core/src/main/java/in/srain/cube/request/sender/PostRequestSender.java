package in.srain.cube.request.sender;

import in.srain.cube.request.IRequest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

public class PostRequestSender extends BaseRequestSender {

    public PostRequestSender(IRequest<?> request, HttpURLConnection httpURLConnection) {
        super(request, httpURLConnection);
    }

    @Override
    public void setup() throws IOException {
        mHttpURLConnection.setRequestMethod("POST");
        mHttpURLConnection.setDoOutput(true);
        mHttpURLConnection.setDoInput(true);
        super.setup();
    }

    @Override
    public void send() throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(mHttpURLConnection.getOutputStream());
        writer.write(mRequestData.getPostString());
        writer.flush();
    }
}
