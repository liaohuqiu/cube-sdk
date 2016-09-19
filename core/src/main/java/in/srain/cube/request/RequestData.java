package in.srain.cube.request;

import android.text.TextUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RequestData {

    private static final String EMPTY = "";
    private static final String CHAR_QM = "?";
    private static final String CHAR_AND = "&";
    private static final String CHAR_EQ = "=";
    private static final String CHAR_SET = "UTF-8";

    public String mUrl;

    private HashMap<String, Object> mQueryData;
    private HashMap<String, Object> mPostData;
    private HashMap<String, Object> mHeaderData;
    private HashMap<String, UploadFileInfo> mUploadFileInfoHashMap;
    private boolean mUsePost = false;
    private String mTag;

    public static String buildQueryString(Map<String, ?> data, String url) {

        if (data == null || data.size() == 0) {
            return url;
        }

        StringBuilder sb = new StringBuilder();
        boolean append = false;
        if (url != null) {
            sb.append(url);
            if (url.contains(CHAR_QM)) {
                append = true;
            } else {
                sb.append(CHAR_QM);
            }
        }
        Iterator<? extends Map.Entry<String, ?>> it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ?> item = it.next();
            if (append) {
                sb.append(CHAR_AND);
            } else {
                append = true;
            }

            try {
                if (TextUtils.isEmpty(item.getKey())) {
                    continue;
                }
                sb.append(URLEncoder.encode(item.getKey(), CHAR_SET));
                sb.append(CHAR_EQ);
                if (item.getValue() != null) {
                    sb.append(URLEncoder.encode(item.getValue().toString(), CHAR_SET));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public RequestData addPostData(String key, Object data) {
        if (mPostData == null) {
            mPostData = new HashMap<String, Object>();
        }
        mPostData.put(key, data);
        return this;
    }

    public RequestData addHeader(String key, Object data) {
        if (mHeaderData == null) {
            mHeaderData = new HashMap<String, Object>();
        }
        mHeaderData.put(key, data);
        return this;
    }

    public RequestData addPostData(Map<String, ?> data) {
        if (mPostData == null) {
            mPostData = new HashMap<String, Object>();
        }
        mPostData.putAll(data);
        return this;
    }

    public RequestData addQueryData(String key, Object data) {
        if (mQueryData == null) {
            mQueryData = new HashMap<String, Object>();
        }
        mQueryData.put(key, data);
        return this;
    }

    public RequestData addQueryData(Map<String, ?> data) {
        if (mQueryData == null) {
            mQueryData = new HashMap<String, Object>();
        }
        mQueryData.putAll(data);
        return this;
    }

    public String getRequestUrl() {
        if (mQueryData != null) {
            return buildQueryString(mQueryData, mUrl);
        }
        return mUrl;
    }

    public RequestData setRequestUrl(String url) {
        mUrl = url;
        return this;
    }

    public HashMap<String, Object> getHeaderData() {
        return mHeaderData;
    }

    public HashMap<String, Object> getQueryData() {
        return mQueryData;
    }

    public HashMap<String, Object> getPostData() {
        return mPostData;
    }

    public RequestData usePost(boolean use) {
        mUsePost = use;
        return this;
    }

    /**
     * add file to be uploaded
     *
     * @param fieldName
     * @param uploadFile, will extract extension from the file name
     * @return
     */
    @SuppressWarnings({"unused"})
    public RequestData addFile(String fieldName, String uploadFile) {
        addFile(fieldName, uploadFile, null);
        return this;
    }

    /**
     * add file to be uploaded
     *
     * @param fieldName
     * @param uploadFile
     * @param fileName   if provided, will use this as filename
     * @return
     */
    @SuppressWarnings({"unused"})
    public RequestData addFile(String fieldName, String uploadFile, String fileName) {
        addFile(fieldName, new File(uploadFile), fileName);
        return this;
    }

    /**
     * @param fieldName
     * @param uploadFile
     * @return
     */
    @SuppressWarnings({"unused"})
    public RequestData addFile(String fieldName, File uploadFile) {
        addFile(fieldName, uploadFile, null);
        return this;
    }

    @SuppressWarnings({"unused"})
    public RequestData addFile(String fieldName, File uploadFile, String fileName) {

        if (mUploadFileInfoHashMap == null) {
            mUploadFileInfoHashMap = new HashMap<String, UploadFileInfo>();
        }

        UploadFileInfo uploadFileInfo = new UploadFileInfo();
        uploadFileInfo.fieldName = fieldName;
        uploadFileInfo.uploadFile = uploadFile;
        uploadFileInfo.fileName = fileName;

        mUploadFileInfoHashMap.put(fieldName, uploadFileInfo);
        return this;
    }

    public String getTag() {
        return mTag;
    }

    /**
     * Set a tag to mark this request
     */
    public RequestData setTag(String tag) {
        mTag = tag;
        return this;
    }

    public String getPostString() {
        if (mPostData == null || mPostData.size() == 0) {
            return EMPTY;
        }
        return buildQueryString(mPostData, null);
    }

    public boolean shouldPost() {
        return mUsePost || (mPostData != null && mPostData.size() > 0) || isMultiPart();
    }

    public HashMap<String, UploadFileInfo> getUploadFiles() {
        return mUploadFileInfoHashMap;
    }

    public boolean isMultiPart() {
        return mUploadFileInfoHashMap != null && mUploadFileInfoHashMap.size() > 0;
    }

    @Override
    public String toString() {
        return String.format("RequestData: [%s, G: %s, P: %s, F: %s]", getRequestUrl(), mQueryData, mPostData, mUploadFileInfoHashMap);
    }

    public static class UploadFileInfo {
        public File uploadFile;
        public String fileName;
        public String fieldName;

        @Override
        public String toString() {
            return String.format("UploadFileInfo:[%s %s %s]", fieldName, fileName, uploadFile);
        }
    }
}
