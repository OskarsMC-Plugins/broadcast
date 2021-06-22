package com.oskarsmc.broadcast.util;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {
    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
}
