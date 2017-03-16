package org.cheeseandbacon.wellsweather;

import java.io.Serializable;

public class WeatherHazard implements Serializable{
    private String text;
    private String url;

    public WeatherHazard(){
        text = JSONParser.INVALID_STRING;
        url = JSONParser.INVALID_STRING;
    }

    public String getFormattedText(){
        return !text.equals(JSONParser.INVALID_STRING) ? text : "";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url.replaceAll("amp;","");
    }
}
