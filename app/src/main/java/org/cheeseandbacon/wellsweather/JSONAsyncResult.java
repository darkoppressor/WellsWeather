package org.cheeseandbacon.wellsweather;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONAsyncResult {
    private static final String TAG = "JSONAsyncResult";
    private int httpStatus;
    private WeatherData weatherData;
    private Location location;

    public JSONAsyncResult(){
        httpStatus = -1;

        weatherData = new WeatherData();

        location = null;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public WeatherData getWeatherData() {
        return weatherData;
    }

    public Location getLocation() {
        return location;
    }

    public void parseJSON(JSONAsyncRequest.Type type, String json){
        if (type == JSONAsyncRequest.Type.GET_COORDINATES) {
            try{
                JSONObject root = new JSONObject(json);

                String status = JSONParser.getJSONString(root,"status");

                if (status.equals("OK")) {
                    JSONArray resultsArray = JSONParser.getJSONArray(root,"results");

                    if (resultsArray != null) {
                        int length = resultsArray.length();

                        if (length > 0) {
                            // There could be more results, but we just use the first one
                            JSONObject firstResult = JSONParser.getJSONObject(resultsArray, 0);

                            if (firstResult != null) {
                                JSONObject geometry = JSONParser.getJSONObject(firstResult, "geometry");

                                if (geometry != null) {
                                    JSONObject locationObject = JSONParser.getJSONObject(geometry, "location");

                                    if (locationObject != null) {
                                        double latitude = JSONParser.getJSONDouble(locationObject, "lat");
                                        double longitude = JSONParser.getJSONDouble(locationObject, "lng");

                                        location = new Location("");
                                        location.setLatitude(latitude);
                                        location.setLongitude(longitude);
                                    }
                                }
                            }
                        }
                    }
                }
                else {
                    Log.e(TAG, "JSON parse error: " + status);
                }
            } catch (JSONException e) {
                Log.e(TAG,"Failed to parse JSON string");
            }
        }
        else if (type == JSONAsyncRequest.Type.GET_WEATHER_DATA) {
            weatherData = new WeatherData();

            weatherData.parseJSON(json);
        }
    }
}
