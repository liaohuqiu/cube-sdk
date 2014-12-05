package in.srain.cube.uri;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import in.srain.cube.request.JsonData;

public class CustomizedUri {

    private static final String KEY = "CUBE_URL";
    private JsonData mQueryData;
    private String mPath = null;
    private String mQuery = null;
    private String mUrl = null;

    public String getPath() {
        return mPath;
    }

    public String getQueryString() {
        return mQuery;
    }

    public JsonData getQueryData() {
        return mQueryData;
    }

    public String getUrl() {
        return mUrl;
    }

    public static CustomizedUri parse(String url, String customizedSchema) {
        if (TextUtils.isEmpty(url)) {
            throw new RuntimeException("url is null");
        }

        if (TextUtils.isEmpty(url)) {
            throw new RuntimeException("url is null");
        }

        if (!url.startsWith(customizedSchema)) {
            return null;
        }
        return new CustomizedUri(url, customizedSchema.length());
    }

    public void writeToBundle(Intent intent) {
        intent.putExtra(KEY, getUrl());
    }

    public static CustomizedUri fromIntent(Intent intent, String schema) {
        if (intent == null) {
            return null;
        }
        String url = intent.getStringExtra(KEY);
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        return parse(url, schema);
    }

    private CustomizedUri(String url, int len) {
        mUrl = url;

        // process segment
        int pos_seg = url.indexOf('#');
        if (pos_seg > 0) {
            url = url.substring(0, pos_seg);
        }

        // process query
        int pos_sp = url.indexOf('?');
        if (pos_sp > 0) {
            mQuery = url.substring(pos_sp + 1);
            url = url.substring(0, pos_sp);
        }
        mPath = url.substring(len);

        if (!TextUtils.isEmpty(mQuery)) {
            parseQuery();
        }
    }

    private void parseQuery() {

        mQueryData = JsonData.newMap();
        String query = mQuery;
        int start = 0;
        do {
            int nextAmpersand = query.indexOf('&', start);

            // to next & or to the end of String
            int end = nextAmpersand != -1 ? nextAmpersand : query.length();

            // no more key-value
            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                break;
            }

            // make sure key is no empty
            if (separator != start) {
                mQueryData.put(Uri.decode(query.substring(start, separator)),
                        Uri.decode(query.substring(separator + 1, end)));
            }
            // Move start to end of name.
            if (nextAmpersand != -1) {
                start = nextAmpersand + 1;
            } else {
                break;
            }
        } while (true);
    }
}
