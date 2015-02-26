package in.srain.cube.request.sender;

import android.text.TextUtils;
import in.srain.cube.request.IRequest;
import in.srain.cube.request.RequestData;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MultiPartRequestSender extends PostRequestSender {

    private static final String LINE_FEED = "\r\n";
    private static final String CHARSET_DEFAULT = "UTF-8";
    private String mBoundary;
    private String mCharset = CHARSET_DEFAULT;

    private OutputStream mOutputStream;
    private PrintWriter mWriter;

    public MultiPartRequestSender(IRequest<?> request, HttpURLConnection httpURLConnection) {
        super(request, httpURLConnection);
    }

    @Override
    public void setup() throws IOException {
        super.setup();
        mBoundary = "===" + System.currentTimeMillis() + "===";
        mHttpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + mBoundary);
        mOutputStream = mHttpURLConnection.getOutputStream();
        mWriter = new PrintWriter(new OutputStreamWriter(mOutputStream), true);
    }

    /**
     * post data to Server
     */
    public void send() throws IOException {

        HashMap<String, RequestData.UploadFileInfo> uploadFiles = mRequestData.getUploadFiles();

        HashMap<String, Object> mPostData = mRequestData.getPostData();
        // send data to Server
        if (mPostData != null && mPostData.size() != 0) {

            Iterator<Map.Entry<String, Object>> iterator = mPostData.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> item = iterator.next();
                Object value = item.getValue();
                if (value == null) {
                    value = "";
                }
                addFormField(item.getKey(), value.toString());
            }
        }

        // send file to server
        Iterator<Map.Entry<String, RequestData.UploadFileInfo>> iterator = uploadFiles.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, RequestData.UploadFileInfo> item = iterator.next();
            RequestData.UploadFileInfo uploadFileInfo = item.getValue();
            addFilePart(uploadFileInfo.fieldName, uploadFileInfo.uploadFile, uploadFileInfo.fileName);
        }
    }

    @Override
    public void getResponse(StringBuilder sb) throws IOException {
        mWriter.append(LINE_FEED).flush();
        mWriter.append("--" + mBoundary + "--").append(LINE_FEED);
        mWriter.close();
        super.getResponse(sb);
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        mWriter.append("--" + mBoundary).append(LINE_FEED);
        mWriter.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        mWriter.append("Content-Type: text/plain; charset=" + mCharset).append(
                LINE_FEED);
        mWriter.append(LINE_FEED);
        mWriter.append(value).append(LINE_FEED);
        mWriter.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @param fileName   the filename field
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile, String fileName) throws IOException {
        if (TextUtils.isEmpty(fileName)) {
            fileName = uploadFile.getName();
        }
        mWriter.append("--" + mBoundary).append(LINE_FEED);
        mWriter.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        mWriter.append(
                "Content-Type: " + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        mWriter.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        mWriter.append(LINE_FEED);
        mWriter.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            mOutputStream.write(buffer, 0, bytesRead);
        }
        mOutputStream.flush();
        inputStream.close();

        mWriter.append(LINE_FEED);
        mWriter.flush();
    }
}