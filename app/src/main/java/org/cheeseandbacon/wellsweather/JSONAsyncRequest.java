package org.cheeseandbacon.wellsweather;

import android.location.Location;

public class JSONAsyncRequest {
	public enum Type {
		GET_WEATHER_DATA, GET_COORDINATES
	}

	private JSONAsyncRequest.Type type;
	private JSONTaskCallbacks callBack;
	private Location location;
	private String locationString;

	public JSONAsyncRequest.Type getType() {
		return type;
	}

	public void setType(JSONAsyncRequest.Type type) {
		this.type = type;
	}

	public JSONTaskCallbacks getCallBack() {
		return callBack;
	}

	public void setCallBack(JSONTaskCallbacks callBack) {
		this.callBack = callBack;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getLocationString() {
		return locationString;
	}

	public void setLocationString(String locationString) {
		this.locationString = locationString;
	}
}
