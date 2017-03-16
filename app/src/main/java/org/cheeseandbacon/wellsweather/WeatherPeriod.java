package org.cheeseandbacon.wellsweather;

import java.io.Serializable;

public class WeatherPeriod implements Serializable {
    private static final String IMAGE_URL_DUAL = "DualImage.php?";

    private int temperature;
    private String weather;
    private String text;
    private int precipitationChance;
    private String name;
    private String temperatureLabel;
    private String weatherImage;
    private WeatherDualImage weatherDualImage;

    private String units;

    public WeatherPeriod(){
        temperature = JSONParser.INVALID_INT;
        weather = JSONParser.INVALID_STRING;
        text = JSONParser.INVALID_STRING;
        precipitationChance = JSONParser.INVALID_INT;
        name = JSONParser.INVALID_STRING;
        temperatureLabel = JSONParser.INVALID_STRING;
        weatherImage = JSONParser.INVALID_STRING;
        weatherDualImage = null;

        units = "";
    }

    public String getFormattedTemperature(){
        return temperature != JSONParser.INVALID_INT ? Units.getTemperature(units, temperature) + Units.getTemperature(units) : "";
    }

    public String getFormattedWeather(){
        return !weather.equals(JSONParser.INVALID_STRING) ? weather : "";
    }

    public String getFormattedText(){
        return !text.equals(JSONParser.INVALID_STRING) ? text : "";
    }

    public String getFormattedPrecipitationChance(){
        return precipitationChance != JSONParser.INVALID_INT ? precipitationChance + "%" : "";
    }

    public String getFormattedName(){
        return !name.equals(JSONParser.INVALID_STRING) ? name : "";
    }

    public String getFormattedTemperatureLabel(){
        return !temperatureLabel.equals(JSONParser.INVALID_STRING) ? temperatureLabel : "";
    }

    public String getFormattedWeatherImage() {
        return !weatherImage.equals(JSONParser.INVALID_STRING) ? weatherImage : "";
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getPrecipitationChance() {
        return precipitationChance;
    }

    public void setPrecipitationChance(int precipitationChance) {
        this.precipitationChance = precipitationChance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemperatureLabel() {
        return temperatureLabel;
    }

    public void setTemperatureLabel(String temperatureLabel) {
        this.temperatureLabel = temperatureLabel;
    }

    public String getWeatherImage() {
        return weatherImage;
    }

    public void setWeatherImage(String weatherImage) {
        loadWeatherDualImage(weatherImage);

        if (weatherDualImage == null) {
            // Remove any URL prefix and the file extension
            this.weatherImage = weatherImage.replaceAll("(.*)/","").replaceAll(".png","");
        } else {
            this.weatherImage = JSONParser.INVALID_STRING;
        }
    }

    public WeatherDualImage getWeatherDualImage() {
        return weatherDualImage;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    private void loadWeatherDualImage(String weatherImage) {
        if (weatherImage.contains(IMAGE_URL_DUAL)) {
            weatherDualImage = new WeatherDualImage();

            // Remove the URL prefix
            String dualImageData = weatherImage.replaceAll("(.*)\\?","");
            // Split the remaining URL into separate values
            String[] dualImageValues = dualImageData.split("&");

            for (String value : dualImageValues) {
                String[] components = value.split("=");

                // We should have two components:
                // a value name, 'i', 'j', 'ip', or 'jp'
                // a string representing the file name component
                if (components.length == 2) {
                    switch (components[0]) {
                        case "i":
                            weatherDualImage.setLeftBaseName(components[1]);
                            break;
                        case "j":
                            weatherDualImage.setRightBaseName(components[1]);
                            break;
                        case "ip":
                            weatherDualImage.setLeftSuffix(components[1]);
                            break;
                        case "jp":
                            weatherDualImage.setRightSuffix(components[1]);
                            break;
                    }
                }
            }
        }
    }
}
