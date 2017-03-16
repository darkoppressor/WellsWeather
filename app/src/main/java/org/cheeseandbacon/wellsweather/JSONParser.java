package org.cheeseandbacon.wellsweather;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {
    private static final String TAG = "JSONParser";
    public static final int INVALID_INT=-999;
    public static final double INVALID_DOUBLE=-999.0;
    public static final String INVALID_STRING="null";

    public static JSONObject getJSONObject(JSONObject object, String key){
        try{
            return object.getJSONObject(key);
        } catch (JSONException e) {
            Log.e(TAG,"Failed to retrieve JSON object for key "+key);
        }

        return null;
    }

    public static JSONArray getJSONArray(JSONObject object, String key){
        try{
            return object.getJSONArray(key);
        } catch (JSONException e) {
            Log.e(TAG,"Failed to retrieve JSON array for key "+key);
        }

        return null;
    }

    public static String getJSONString(JSONObject object, String key){
        try{
            return object.getString(key).trim();
        } catch (JSONException e) {
            Log.e(TAG,"Failed to retrieve JSON string for key "+key);
        }

        return INVALID_STRING;
    }

    public static int getJSONInt(JSONObject object, String key){
        try{
            return object.getInt(key);
        } catch (JSONException e) {
            Log.e(TAG,"Failed to retrieve JSON int for key "+key);
        }

        return INVALID_INT;
    }

    public static double getJSONDouble(JSONObject object, String key){
        try{
            return object.getDouble(key);
        } catch (JSONException e) {
            Log.e(TAG,"Failed to retrieve JSON double for key "+key);
        }

        return INVALID_DOUBLE;
    }

    public static JSONObject getJSONObject(JSONArray array, int index){
        try{
            return array.getJSONObject(index);
        } catch (JSONException e) {
            Log.e(TAG,"Failed to retrieve JSON object from array for index "+index);
        }

        return null;
    }

    public static String getJSONString(JSONArray array, int index){
        try{
            return array.getString(index).trim();
        } catch (JSONException e) {
            Log.e(TAG,"Failed to retrieve JSON string from array for index "+index);
        }

        return INVALID_STRING;
    }

    public static int getJSONInt(JSONArray array, int index){
        try{
            return array.getInt(index);
        } catch (JSONException e) {
            Log.e(TAG,"Failed to retrieve JSON int from array for index "+index);
        }

        return INVALID_INT;
    }
}
