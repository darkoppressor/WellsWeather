package org.cheeseandbacon.wellsweather;

import java.io.Serializable;

public class WeatherDualImage implements Serializable {
    private String leftBaseName;
    private String rightBaseName;
    private String leftSuffix;
    private String rightSuffix;

    WeatherDualImage() {
        leftBaseName = "";
        rightBaseName = "";
        leftSuffix = "";
        rightSuffix = "";
    }

    public String getLeftBaseName() {
        return leftBaseName;
    }

    public String getRightBaseName() {
        return rightBaseName;
    }

    public String getLeftSuffix() {
        return leftSuffix;
    }

    public String getRightSuffix() {
        return rightSuffix;
    }

    public void setLeftBaseName(String leftBaseName) {
        this.leftBaseName = leftBaseName;
    }

    public void setRightBaseName(String rightBaseName) {
        this.rightBaseName = rightBaseName;
    }

    public void setLeftSuffix(String leftSuffix) {
        this.leftSuffix = leftSuffix;
    }

    public void setRightSuffix(String rightSuffix) {
        this.rightSuffix = rightSuffix;
    }
}
