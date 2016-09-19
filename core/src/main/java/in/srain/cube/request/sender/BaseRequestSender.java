package in.srain.cube.request.sender;

import in.srain.cube.request.IRequest;
import in.srain.cube.request.RequestData;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class BaseRequestSender implements IRequestSender {

    protected final static String EMPTY_VALUE = "";
    protected HttpURLConnection mHttpURLConnection;
    protected RequestData mRequestData;
    protected IRequest<?> mRequest;

    public BaseRequestSender(IRequest<?> request, HttpURLConnection httpURLConnection) {
        mRequest = request;
        mHttpURLConnection = httpURLConnection;
        mRequestData = request.getRequestData();
    }

    @Override
    public void setup() throws IOException {
        HashMap<String, Object> data = mRequestData.getHeaderData();
        if (data == null || data.size() == 0) {
            return;
        }
        Iterator<Map.Entry<String, Object>> it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> item = it.next();
            String key = item.getKey();
            Object value = item.getValue();
            if (value == null) {
                value = EMPTY_VALUE;
            }
            mHttpURLConnection.setRequestProperty(key, value.toString());
        }
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public void getResponse(StringBuilder sb) throws IOException {

        // checks server's status code first
        int status = mHttpURLConnection.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            InputStream ips = new BufferedInputStream(mHttpURLConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(ips, "UTF-8"));

            char[] buffer = new char[1024];
            int bufferLength;
            while ((bufferLength = reader.read(buffer, 0, buffer.length)) > 0) {
                sb.append(buffer, 0, bufferLength);
            }
            reader.close();
            ips.close();
            mHttpURLConnection.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
    }
}
