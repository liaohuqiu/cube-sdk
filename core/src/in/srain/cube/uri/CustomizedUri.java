package in.srain.cube.uri;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import in.srain.cube.request.JsonData;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;

public class CustomizedUri {

    private static final String KEY = "CUBE_URL";
    private static final String[] PUB_SCHEMA_LIST = new String[]{"http://", "https://"};
    private final static int TAG_MASK_IS_CUSTOMIZED = 0x1;
    private final static int TAG_MASK_IS_HTTP = 0x1 << 1;
    private JsonData mQueryData = JsonData.newMap();
    private String mSchemaPre = null;
    private String mPath = null;
    private String mQuery = null;
    private String mOriginUrl = null;
    private int mTag = 0;

    private CustomizedUri(String url, String schemaPre) {
        mSchemaPre = schemaPre;
        mOriginUrl = url;

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
        mPath = url.substring(schemaPre.length());

        if (!TextUtils.isEmpty(mQuery)) {
            parseQuery();
        }
    }

    public static CustomizedUri parse(String url, String customizedSchema) {
        if (TextUtils.isEmpty(url)) {
            throw new RuntimeException("url is null");
        }

        url = url.trim();

        if (url.startsWith(customizedSchema)) {
            CustomizedUri uri = new CustomizedUri(url, customizedSchema);
            uri.mTag |= TAG_MASK_IS_CUSTOMIZED;
            return uri;
        } else {
            for (int i = 0; i < PUB_SCHEMA_LIST.length; i++) {
                String schema = PUB_SCHEMA_LIST[i];
                if (url.startsWith(schema)) {
                    CustomizedUri uri = new CustomizedUri(url, schema);
                    uri.mTag |= TAG_MASK_IS_HTTP;
                    return uri;
                }
            }
        }
        return null;
    }

    private static String decode(String content, String encoding) {
        try {
            return URLDecoder.decode(content, encoding != null ? encoding : "ISO-8859-1");
        } catch (UnsupportedEncodingException var3) {
            throw new IllegalArgumentException(var3);
        }
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

    public String getPath() {
        return mPath;
    }

    public boolean isCustomized() {
        return (mTag & TAG_MASK_IS_CUSTOMIZED) > 0;
    }

    public boolean isHttpSchema() {
        return (mTag & TAG_MASK_IS_HTTP) > 0;
    }

    public String getQueryString() {
        return mQuery;
    }

    public JsonData getQueryData() {
        return mQueryData;
    }

    public String getOriginUrl() {
        return mOriginUrl;
    }

    public void writeToBundle(Intent intent) {
        intent.putExtra(KEY, buildUrl());
        if (mQueryData != null && mQueryData.length() > 0) {
            Iterator<String> names = mQueryData.keys();
            while (names.hasNext()) {
                String name = names.next();
                String value = mQueryData.optString(name);
                intent.putExtra(name, value);
            }
        }
    }

    public String buildUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(mSchemaPre).append(mPath);

        if (mQueryData != null && mQueryData.length() > 0) {
            sb.append("?");
            Iterator<String> names = mQueryData.keys();
            boolean first = true;
            while (names.hasNext()) {
                String name = names.next();
                String encodedName = Uri.encode(name);
                String value = mQueryData.optString(name);
                String encodedValue = value != null ? Uri.encode(value) : "";
                if (first) {
                    first = false;
                } else {
                    sb.append("&");
                }

                sb.append(encodedName);
                sb.append("=");
                sb.append(encodedValue);
            }
        }
        return sb.toString();
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
