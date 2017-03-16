package org.cheeseandbacon.wellsweather;

import static java.lang.Math.round;

public class Units {
    // For conversion, all values are assumed to begin in non-SI units

    private static final String SI = "si";

    public static String getTemperature(String units){
        return units.equals(SI) ? "\u2103" : "\u2109";
    }

    public static String getDistanceSmall(String units){
        return units.equals(SI) ? "m" : "ft";
    }

    public static String getDistanceLarge(String units){
        return units.equals(SI) ? "km" : "mi";
    }

    public static String getSpeed(String units){
        return units.equals(SI) ? "kph" : "mph";
    }

    public static String getPressure(String units){
        return units.equals(SI) ? "hPa" : "mb";
    }

    public static int getTemperature(String units, int temperature){
        return units.equals(SI) ? (int)round((temperature - 32) / 1.8) : temperature;
    }

    public static int getDistanceSmall(String units, int distance){
        return units.equals(SI) ? (int)round(distance * 0.3048) : distance;
    }

    public static double getDistanceLarge(String units, double distance){
        return units.equals(SI) ? distance * 1.60934 : distance;
    }

    public static int getSpeed(String units, int speed){
        return units.equals(SI) ? (int)round(speed * 1.60934) : speed;
    }

    public static double getPressure(String units, double pressure){
        return units.equals(SI) ? pressure : pressure;
    }
}
