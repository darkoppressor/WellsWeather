package org.cheeseandbacon.wellsweather;

import android.location.Location;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

public class JSONService {
	private static final String TAG = "JSONService";
	//ms
	private static final Integer CONNECTION_TIMEOUT = 10000;
	private static final Integer READ_TIMEOUT = 10000;

	private static final String URL_TEMPLATE_FORECAST = "http://forecast.weather.gov/MapClick.php?lat=LATITUDE&lon=LONGITUDE&FcstType=json";
	private static final String URL_TEMPLATE_GEOCODE = "https://maps.googleapis.com/maps/api/geocode/json?address=ADDRESS&key=API_KEY";

	private int httpStatus;

	public JSONService(){
		httpStatus = -1;
	}

	private String retrieveJSON(String urlText){
		String resultText = "";

		try {
			URL url = new URL(urlText);

			Log.d(TAG, "URL is " + urlText);

			try {
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

				urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
				urlConnection.setReadTimeout(READ_TIMEOUT);

				try {
					InputStream in = new BufferedInputStream(urlConnection.getInputStream());

					int c = -1;

					do {
						try {
							c = in.read();
						} catch (IOException e) {
							Log.e(TAG, "Error reading HTTP result from input stream");
						}

						if (c != -1) {
							resultText += (char) c;
						}
					} while (c != -1);

					httpStatus = urlConnection.getResponseCode();

					// Note that this String may be too long to all show in the log
					//Log.d(TAG,"JSON string result: " + resultText);
				} catch (SocketTimeoutException e) {
					Log.e(TAG, "Connection timed out");
				} catch (IOException e) {
					Log.e(TAG, "Error opening connection");
				} finally {
					urlConnection.disconnect();
				}
			} catch (IOException e) {
				Log.e(TAG, "Failed to create connection");
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "URL was malformed");
		}

		return resultText;
	}

	public JSONAsyncResult execute(JSONAsyncRequest request) {
		JSONAsyncResult result = new JSONAsyncResult();

		String urlText = "";

		if (request.getType() == JSONAsyncRequest.Type.GET_COORDINATES) {
			String apiKey = BuildConfig.API_KEY;

			String address = request.getLocationString();

			String encoding = "UTF-8";

			try {
				apiKey = URLEncoder.encode(apiKey, encoding);
				address = URLEncoder.encode(address, encoding);
			} catch (UnsupportedEncodingException e) {
				Log.d(TAG, "Unsupported encoding: " + encoding);
			}

			urlText = URL_TEMPLATE_GEOCODE;
			urlText = urlText.replace("API_KEY", apiKey);
			urlText = urlText.replace("ADDRESS", address);
		}
		else if (request.getType() == JSONAsyncRequest.Type.GET_WEATHER_DATA) {
			Location location = request.getLocation();

			urlText = URL_TEMPLATE_FORECAST;
			urlText = urlText.replace("LATITUDE", Double.toString(location.getLatitude()));
			urlText = urlText.replace("LONGITUDE", Double.toString(location.getLongitude()));
		}

		String json = retrieveJSON(urlText);

		result.setHttpStatus(httpStatus);

		if (httpStatus != HttpURLConnection.HTTP_OK) {
			Log.e(TAG, "HTTP status code: " + httpStatus);

			return result;
		}

		result.parseJSON(request.getType(), json);

		return result;
	}
}
