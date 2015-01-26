package in.srain.cube.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RequestData {

    private static final String CHAR_QM = "?";
    private static final String CHAR_AND = "&";
    private static final String CHAR_EQ = "=";
    private static final String CHAR_SET = "UTF-8";

    public String mUrl;

    private HashMap<String, Object> mQueryData;
    private HashMap<String, Object> mPostData;
    private boolean mUsePost = false;

    public static String buildQueryString(Map<String, ?> data, String url) {

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
                sb.append(URLEncoder.encode(item.getKey(), CHAR_SET));
                sb.append(CHAR_EQ);
                sb.append(URLEncoder.encode(item.getValue().toString(), CHAR_SET));
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

    public String getPostString() {
        return buildQueryString(mPostData, null);
    }

    public boolean shouldPost() {
        return mUsePost || (mPostData != null && mPostData.size() > 0);
    }
}
