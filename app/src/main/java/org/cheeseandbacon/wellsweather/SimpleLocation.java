package org.cheeseandbacon.wellsweather;

import android.location.Location;

import java.io.Serializable;

public class SimpleLocation implements Serializable {
    private double latitude;
    private double longitude;

    public SimpleLocation(Location location){
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    public Location toLocation(){
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location;
    }
}
