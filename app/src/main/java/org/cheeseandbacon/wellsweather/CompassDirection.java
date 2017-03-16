package org.cheeseandbacon.wellsweather;

public final class CompassDirection {
    private static final String[] DIRECTIONS = {
            "N",
            "NNE",
            "NE",
            "ENE",
            "E",
            "ESE",
            "SE",
            "SSE",
            "S",
            "SSW",
            "SW",
            "WSW",
            "W",
            "WNW",
            "NW",
            "NNW",
            "N"
    };

    public static String convert(int degreesTrue){
        degreesTrue %= 360;

        if (degreesTrue < 0) {
            degreesTrue += 360;
        }

        int index = (int)Math.round((double)degreesTrue / 22.5);

        return DIRECTIONS[index];
    }
}
