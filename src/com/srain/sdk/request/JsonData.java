package com.srain.sdk.request;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

@SuppressWarnings("rawtypes")
public class JsonData {

	private Object mJson;

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

	public int optInt(int index) {
		return optArrayOrNew().optInt(index);
	}

	public int optInt(String name) {
		return optMapOrNew().optInt(name);
	}

	public String optString(int index) {
		return optArrayOrNew().optString(index);
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

	public String toString() {
		if (mJson instanceof JSONArray) {
			return ((JSONArray) mJson).toString();
		}
		if (mJson instanceof JSONObject) {
			return ((JSONObject) mJson).toString();
		}
		return null;
	}
}
