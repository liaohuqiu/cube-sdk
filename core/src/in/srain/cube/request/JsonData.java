package in.srain.cube.request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.*;

@SuppressWarnings("rawtypes")
public final class JsonData {

    private static final String EMPTY_STRING = "";
    private static final JSONArray EMPTY_JSON_ARRAY = new JSONArray();
    private static final JSONObject EMPTY_JSON_OBJECT = new JSONObject();
    private Object mJson;

    public static JsonData newMap() {
        return create(new HashMap<String, Object>());
    }

    public static JsonData newList() {
        return create(new ArrayList<Object>());
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
        if (o instanceof Collection) {
            json.mJson = new JSONArray((Collection) o);
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

    public String optString(String name, String fallback) {
        return optMapOrNew().optString(name, fallback);
    }

    public String optString(int index) {
        return optArrayOrNew().optString(index);
    }

    public String optString(int index, String fallback) {
        return optArrayOrNew().optString(index, fallback);
    }

    public int optInt(String name) {
        return optMapOrNew().optInt(name);
    }

    public int optInt(String name, int fallback) {
        return optMapOrNew().optInt(name, fallback);
    }

    public int optInt(int index) {
        return optArrayOrNew().optInt(index);
    }

    public int optInt(int index, int fallback) {
        return optArrayOrNew().optInt(index, fallback);
    }

    public boolean optBoolean(String name) {
        return optMapOrNew().optBoolean(name);
    }

    public boolean optBoolean(String name, boolean fallback) {
        return optMapOrNew().optBoolean(name, fallback);
    }

    public boolean optBoolean(int index) {
        return optArrayOrNew().optBoolean(index);
    }

    public boolean optBoolean(int index, boolean fallback) {
        return optArrayOrNew().optBoolean(index, fallback);
    }

    public double optDouble(String name) {
        return optMapOrNew().optDouble(name);
    }

    public double optDouble(String name, double fallback) {
        return optMapOrNew().optDouble(name, fallback);
    }

    public double optDouble(int index) {
        return optArrayOrNew().optDouble(index);
    }

    public double optDouble(int index, double fallback) {
        return optArrayOrNew().optDouble(index, fallback);
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
        return EMPTY_JSON_OBJECT;
    }

    private Object valueForPut(Object value) {
        if (value instanceof JsonData) {
            return ((JsonData) value).getRawData();
        } else {
            return value;
        }
    }

    public void put(String key, Object value) {
        if (mJson instanceof JSONObject) {
            try {
                ((JSONObject) mJson).put(key, valueForPut(value));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void put(Object value) {
        if (mJson instanceof JSONArray) {
            ((JSONArray) mJson).put(valueForPut(value));
        }
    }

    public void put(int index, Object value) {
        if (mJson instanceof JSONArray) {
            try {
                ((JSONArray) mJson).put(index, valueForPut(value));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public JsonData editMap(int index) {
        if (has(index)) {
            return optJson(index);
        }
        JsonData map = JsonData.newMap();
        put(index, map);
        return map;
    }

    public JsonData editMap() {
        JsonData map = JsonData.newMap();
        put(map);
        return map;
    }

    public JsonData editMap(String key) {
        if (has(key)) {
            return optJson(key);
        }
        JsonData map = JsonData.newMap();
        put(key, map);
        return map;
    }

    public JsonData editList(String key) {
        if (has(key)) {
            return optJson(key);
        }
        JsonData list = JsonData.newList();
        put(key, list);
        return list;
    }

    public JsonData editList(int index) {
        if (has(index)) {
            return optJson(index);
        }
        JsonData list = JsonData.newList();
        put(index, list);
        return list;
    }

    public JsonData editList() {
        JsonData list = JsonData.newList();
        put(list);
        return list;
    }

    public JSONArray optArrayOrNew() {
        if (mJson instanceof JSONArray) {
            return (JSONArray) mJson;
        }
        return EMPTY_JSON_ARRAY;
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

    public <T> ArrayList<T> asList(JsonConverter<T> converter) {
        ArrayList<T> arrayList = new ArrayList<T>();
        if (mJson instanceof JSONArray) {
            final JSONArray array = (JSONArray) mJson;
            for (int i = 0; i < array.length(); i++) {
                arrayList.add(converter.convert(JsonData.create(array.opt(i))));
            }
        } else if (mJson instanceof JSONObject) {
            final JSONObject json = (JSONObject) mJson;

            Iterator it = json.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                arrayList.add(converter.convert(JsonData.create(json.opt(key))));
            }
        }
        return arrayList;
    }

    public <T> ArrayList<T> asList() {
        ArrayList<T> arrayList = new ArrayList<T>();
        if (mJson instanceof JSONArray) {
            final JSONArray array = (JSONArray) mJson;
            for (int i = 0; i < array.length(); i++) {
                arrayList.add((T) array.opt(i));
            }
        } else if (mJson instanceof JSONObject) {
            final JSONObject json = (JSONObject) mJson;

            Iterator it = json.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                arrayList.add((T) json.opt(key));
            }
        }
        return arrayList;
    }

    public interface JsonConverter<T> {
        public T convert(JsonData item);
    }
}
