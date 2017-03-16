package org.cheeseandbacon.wellsweather;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class WeatherData implements Serializable{
    private static final String TAG = "WeatherData";
    private static final double MB_PER_INHG = 33.8639;

    private boolean valid;

    private String creationDate;
    private String source;
    private int relativeHumidity;
    private int dewPoint;
    private int windDirection;
    private int windSpeed;
    private int windGusts;
    private int windChill;
    private int heatIndex;
    private double atmosphericPressure;
    private double latitude;
    private double longitude;
    private int elevation;
    private double visibility;

    private WeatherPeriod currentConditions;
    private WeatherPeriod[] periods;
    private WeatherHazard[] hazards;

    // The type of units to use for display
    private String units;

    private void clampWindDirection(){
        // A wind direction of 999 in JSON represents variable direction
        if (windDirection == 999) {
            windDirection = JSONParser.INVALID_INT;
        }
        else if (windDirection != JSONParser.INVALID_INT) {
            windDirection %= 360;

            if (windDirection < 0) {
                windDirection += 360;
            }
        }
    }

    private void calculateHeatIndex(){
        if (windChill == JSONParser.INVALID_INT &&
                currentConditions.getTemperature() != JSONParser.INVALID_INT &&
                relativeHumidity != JSONParser.INVALID_INT) {
            double T = currentConditions.getTemperature();
            double RH = relativeHumidity;

            // Calculate HI using the simple formula
            double simpleHI = 0.5 * (T + 61.0 + ((T - 68.0) * 1.2) + (RH * 0.094));

            // Average the simple HI with the temperature
            simpleHI += T;
            simpleHI /= 2.0;

            if (simpleHI < 80.0) {
                heatIndex = (int)simpleHI;
            }
            else{
                double complexHI = -42.379 + 2.04901523 * T + 10.14333127 * RH -
                        .22475541 * T * RH - .00683783 * T * T - .05481717 * RH * RH +
                        .00122874 * T * T * RH + .00085282 * T * RH * RH -
                        .00000199 * T * T * RH * RH;

                if (RH < 13.0 && T >= 80.0 && T <= 112.0) {
                    complexHI -= ((13.0 - RH) / 4.0) * Math.sqrt((17.0 - Math.abs(T - 95.0)) / 17.0);
                }
                else if(RH > 85.0 && T >= 80.0 && T <= 87.0){
                    complexHI += ((RH - 85.0) / 10.0) * ((87.0 - T) / 5.0);
                }

                heatIndex = (int)complexHI;
            }
        }
    }

    public WeatherData(){
        valid = false;

        creationDate = JSONParser.INVALID_STRING;
        source = JSONParser.INVALID_STRING;
        relativeHumidity = JSONParser.INVALID_INT;
        dewPoint = JSONParser.INVALID_INT;
        windDirection = JSONParser.INVALID_INT;
        windSpeed = JSONParser.INVALID_INT;
        windGusts = JSONParser.INVALID_INT;
        windChill = JSONParser.INVALID_INT;
        heatIndex = JSONParser.INVALID_INT;
        atmosphericPressure = JSONParser.INVALID_DOUBLE;
        latitude = JSONParser.INVALID_DOUBLE;
        longitude = JSONParser.INVALID_DOUBLE;
        elevation = JSONParser.INVALID_INT;
        visibility = JSONParser.INVALID_DOUBLE;

        currentConditions = new WeatherPeriod();
        periods = null;
        hazards = null;

        units = "";
    }

    public void parseJSON(String json){
        try{
            JSONObject root = new JSONObject(json);

            creationDate = JSONParser.getJSONString(root,"creationDateLocal");

            JSONObject currentObservation = JSONParser.getJSONObject(root,"currentobservation");

            if(currentObservation != null){
                source = JSONParser.getJSONString(currentObservation, "name") + " (" +
                        JSONParser.getJSONString(currentObservation, "id") + ")";
                currentConditions.setTemperature(JSONParser.getJSONInt(currentObservation,"Temp"));
                currentConditions.setWeather(JSONParser.getJSONString(currentObservation,"Weather"));
                currentConditions.setWeatherImage(JSONParser.getJSONString(currentObservation,"Weatherimage"));
                relativeHumidity = JSONParser.getJSONInt(currentObservation,"Relh");
                dewPoint = JSONParser.getJSONInt(currentObservation,"Dewp");
                windDirection = JSONParser.getJSONInt(currentObservation,"Windd");
                windSpeed = JSONParser.getJSONInt(currentObservation,"Winds");
                windGusts = JSONParser.getJSONInt(currentObservation,"Gust");
                windChill = JSONParser.getJSONInt(currentObservation,"WindChill");
                atmosphericPressure = JSONParser.getJSONDouble(currentObservation,"Altimeter");

                // If our preferred pressure value in mb is unavailable, try falling back
                // to the inHg value
                if(atmosphericPressure == JSONParser.INVALID_DOUBLE){
                    atmosphericPressure = JSONParser.getJSONDouble(currentObservation,"SLP");

                    if(atmosphericPressure != JSONParser.INVALID_DOUBLE){
                        // Convert the inHg value to mb
                        atmosphericPressure *= MB_PER_INHG;
                    }
                }

                latitude = JSONParser.getJSONDouble(currentObservation,"latitude");
                longitude = JSONParser.getJSONDouble(currentObservation,"longitude");
                elevation = JSONParser.getJSONInt(currentObservation,"elev");
                visibility = JSONParser.getJSONDouble(currentObservation,"Visibility");
            }

            JSONArray tempArray = null;
            JSONArray weatherArray = null;
            JSONArray weatherImageArray = null;
            JSONArray textArray = null;
            JSONArray precipArray = null;
            JSONArray nameArray = null;
            JSONArray tempLabelArray = null;

            JSONObject data = JSONParser.getJSONObject(root,"data");

            if(data != null) {
                JSONArray hazardArray = JSONParser.getJSONArray(data,"hazard");
                JSONArray hazardURLArray = JSONParser.getJSONArray(data,"hazardUrl");

                if(hazardArray != null && hazardURLArray != null && hazardArray.length() == hazardURLArray.length()){
                    int length = hazardArray.length();

                    if (length > 0) {
                        hazards = new WeatherHazard[length];

                        for (int i = 0; i < length; i++) {
                            hazards[i] = new WeatherHazard();
                            hazards[i].setText(JSONParser.getJSONString(hazardArray,i));
                            hazards[i].setUrl(JSONParser.getJSONString(hazardURLArray,i));
                        }
                    }
                }

                tempArray = JSONParser.getJSONArray(data,"temperature");
                weatherArray = JSONParser.getJSONArray(data,"weather");
                weatherImageArray = JSONParser.getJSONArray(data,"iconLink");
                textArray = JSONParser.getJSONArray(data,"text");
                precipArray = JSONParser.getJSONArray(data,"pop");
            }

            JSONObject time = JSONParser.getJSONObject(root,"time");

            if(time != null) {
                nameArray = JSONParser.getJSONArray(time,"startPeriodName");
                tempLabelArray = JSONParser.getJSONArray(time,"tempLabel");
            }

            if(tempArray != null && weatherArray != null && weatherImageArray != null &&
                    textArray != null && precipArray != null && nameArray != null &&
                    tempLabelArray != null &&
                    tempArray.length() == weatherArray.length() &&
                    tempArray.length() == weatherImageArray.length() &&
                    tempArray.length() == textArray.length() &&
                    tempArray.length() == precipArray.length() &&
                    tempArray.length() == nameArray.length() &&
                    tempArray.length() == tempLabelArray.length()){
                int length = tempArray.length();

                if (length > 0) {
                    periods = new WeatherPeriod[length];

                    for (int i = 0; i < length; i++) {
                        periods[i] = new WeatherPeriod();
                        periods[i].setTemperature(JSONParser.getJSONInt(tempArray,i));
                        periods[i].setWeather(JSONParser.getJSONString(weatherArray,i));
                        periods[i].setWeatherImage(JSONParser.getJSONString(weatherImageArray,i));
                        periods[i].setText(JSONParser.getJSONString(textArray,i));
                        periods[i].setPrecipitationChance(JSONParser.getJSONInt(precipArray,i));
                        periods[i].setName(JSONParser.getJSONString(nameArray,i));
                        periods[i].setTemperatureLabel(JSONParser.getJSONString(tempLabelArray,i));
                    }
                }
            }

            clampWindDirection();
            calculateHeatIndex();

            valid = true;
        } catch (JSONException e) {
            Log.e(TAG,"Failed to parse JSON string");
        }
    }

    public String getFormattedCreationDate() {
        return "<b>Station last updated:</b> " + (!creationDate.equals(JSONParser.INVALID_STRING) ? creationDate : "Unknown");
    }

    public String getFormattedSource() {
        return !source.equals(JSONParser.INVALID_STRING) ? source : "Unknown";
    }

    public String getFormattedCurrentTemperature() {
        return currentConditions.getFormattedTemperature();
    }

    public String getFormattedCurrentWeather() {
        return currentConditions.getFormattedWeather();
    }

    public String getFormattedCurrentWeatherImage() {
        return currentConditions.getFormattedWeatherImage();
    }

    public WeatherDualImage getCurrentWeatherDualImage() {
        return currentConditions.getWeatherDualImage();
    }

    public String getFormattedRelativeHumidity() {
        return relativeHumidity != JSONParser.INVALID_INT ? "<b>Relative humidity:</b> " + relativeHumidity + "%" : "";
    }

    public String getFormattedDewPoint() {
        return dewPoint != JSONParser.INVALID_INT ? "<b>Dew point:</b> " + Units.getTemperature(units, dewPoint) + Units.getTemperature(units) : "";
    }

    public String getFormattedWind() {
        if (windDirection != JSONParser.INVALID_INT || windSpeed != JSONParser.INVALID_INT || windGusts != JSONParser.INVALID_INT) {
            if(windSpeed == 0){
                return "<b>Wind speed:</b> Calm";
            }
            else {
                return "<b>Wind speed:</b> " + (windDirection != JSONParser.INVALID_INT ? CompassDirection.convert(windDirection) : "Variable") +
                        (windSpeed != JSONParser.INVALID_INT ? " " + Units.getSpeed(units, windSpeed) + " " + Units.getSpeed(units) : "") +
                        (windGusts != JSONParser.INVALID_INT && windGusts > 0 ? " gusts of " + Units.getSpeed(units, windGusts) + " " + Units.getSpeed(units) : "");
            }
        }
        else{
            return "";
        }
    }

    public String getFormattedApparentTemperature() {
        return windChill != JSONParser.INVALID_INT ?
                "<b>Wind chill:</b> " + Units.getTemperature(units, windChill) +
                        Units.getTemperature(units) :
                (heatIndex != JSONParser.INVALID_INT ? "<b>Heat index:</b> " +
                        Units.getTemperature(units, heatIndex) + Units.getTemperature(units) : "");
    }

    public String getFormattedAtmosphericPressure() {
        return atmosphericPressure != JSONParser.INVALID_DOUBLE ?
                "<b>Atmospheric pressure:</b> " + Units.getPressure(units, atmosphericPressure) + " " + Units.getPressure(units) : "";
    }

    public String getFormattedPosition() {
        if (latitude != JSONParser.INVALID_DOUBLE || longitude != JSONParser.INVALID_DOUBLE || elevation != JSONParser.INVALID_INT) {
            return (latitude != JSONParser.INVALID_DOUBLE ? "Latitude: " + latitude : "") +
                    (longitude != JSONParser.INVALID_DOUBLE ? "  Longitude: " + longitude : "") +
                    (elevation != JSONParser.INVALID_INT ? "  Elevation: " + Units.getDistanceSmall(units, elevation) + " " + Units.getDistanceSmall(units) : "");
        }
        else{
            return "";
        }
    }

    public String getFormattedVisibility() {
        return visibility != JSONParser.INVALID_DOUBLE ? "<b>Visibility:</b> " + Units.getDistanceLarge(units, visibility) + " " + Units.getDistanceLarge(units) : "";
    }

    public boolean isValid(){
        return valid;
    }

    public WeatherPeriod[] getPeriods(){
        return periods;
    }

    public WeatherHazard[] getHazards(){
        return hazards;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;

        currentConditions.setUnits(units);

        for (WeatherPeriod period : periods) {
            period.setUnits(units);
        }
    }
}
