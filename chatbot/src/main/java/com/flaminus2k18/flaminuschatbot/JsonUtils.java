package com.flaminus2k18.flaminuschatbot;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jaisel on 17/1/18.
 */

public class JsonUtils {
    static String getString(JSONObject jsonObject, String name, String defaultValue) {
        try {
            return jsonObject.getString(name);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
    static boolean getBoolean(JSONObject jsonObject, String name, boolean defaultValue){
        try {
            return jsonObject.getBoolean(name);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
}
