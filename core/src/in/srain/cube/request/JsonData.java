package in.srain.cube.request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class JsonData {

    private Object mJson;
    private static final String EMPTY_STRING = "";

    public static JsonData newMap() {
        return create(new HashMap<String, Object>());
    }

    public static JsonData create(String str) {
        Object object = null;
        if (str != null && str.length() >= 0) {
            try {
                JSONTokener jsonTokener = new JSONTokener(str);
                object = jsonTokener.nextValue();
            } catch (Exception e) {
            }
        }
        return create(object);
    }

    public static JsonData create(Object o) {
        JsonData json = new JsonData();
        if (o instanceof JSONArray || o instanceof JSONObject) {
            json.mJson = o;
        }
        if (o instanceof Map) {
            json.mJson = new JSONObject((Map) o);
        }
        return json;
    }

    public Object getRawData() {
        return mJson;
    }

    public JsonData optJson(String name) {

        Object ret = null;
        if (mJson instanceof JSONObject) {
            ret = ((JSONObject) mJson).opt(name);
        }
        return JsonData.create(ret);
    }

    public JsonData optJson(int index) {

        Object ret = null;
        if (mJson instanceof JSONArray) {
            ret = ((JSONArray) mJson).opt(index);
        }
        return JsonData.create(ret);
    }

    public String optString(String name) {
        return optMapOrNew().optString(name);
    }

    public String optString(int index) {
        return optArrayOrNew().optString(index);
    }

    public int optInt(String name) {
        return optMapOrNew().optInt(name);
    }

    public int optInt(int index) {
        return optArrayOrNew().optInt(index);
    }

    public boolean optBoolean(String name) {
        return optMapOrNew().optBoolean(name);
    }

    public boolean optBoolean(int index) {
        return optArrayOrNew().optBoolean(index);
    }

    public double optDouble(String name) {
        return optMapOrNew().optDouble(name);
    }

    public double optDouble(int index) {
        return optArrayOrNew().optDouble(index);
    }

    public boolean has(String name) {
        return optMapOrNew().has(name);
    }

    public boolean has(int index) {
        return optArrayOrNew().length() > index;
    }

    public JSONObject optMapOrNew() {
        if (mJson instanceof JSONObject) {
            return (JSONObject) mJson;
        }
        return new JSONObject();
    }

    public void put(String key, Object value) {
        if (mJson instanceof JSONObject) {
            try {
                ((JSONObject) mJson).put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void put(Object value) {
        if (mJson instanceof JSONArray) {
            ((JSONArray) mJson).put(value);
        }
    }

    public void put(int index, Object value) {
        if (mJson instanceof JSONArray) {
            try {
                ((JSONArray) mJson).put(index, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public JSONArray optArrayOrNew() {
        if (mJson instanceof JSONArray) {
            return (JSONArray) mJson;
        }
        return new JSONArray();
    }

    public int length() {
        if (mJson instanceof JSONArray) {
            return ((JSONArray) mJson).length();
        }
        if (mJson instanceof JSONObject) {
            return ((JSONObject) mJson).length();
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    public Iterator<String> keys() {
        return optMapOrNew().keys();
    }

    public String toString() {
        if (mJson instanceof JSONArray) {
            return ((JSONArray) mJson).toString();
        } else if (mJson instanceof JSONObject) {
            return ((JSONObject) mJson).toString();
        }
        return EMPTY_STRING;
    }

    public ArrayList<JsonData> toArrayList() {
        ArrayList<JsonData> arrayList = new ArrayList<JsonData>();
        if (mJson instanceof JSONArray) {
            final JSONArray array = (JSONArray) mJson;
            for (int i = 0; i < array.length(); i++) {
                arrayList.add(i, JsonData.create(array.opt(i)));
            }
        } else if (mJson instanceof JSONObject) {
            final JSONObject json = (JSONObject) mJson;

            Iterator it = json.keys();

            while (it.hasNext()) {
                String key = (String) it.next();
                arrayList.add(JsonData.create(json.opt(key)));
            }
        }
        return arrayList;
    }
}
