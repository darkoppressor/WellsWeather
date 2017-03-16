package org.cheeseandbacon.wellsweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements JSONTaskCallbacks,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = "MainActivity";
    private static final String LAST_LOCATION = "lastLocation";
    private static final String LAST_MANUAL_LOCATION_STRING = "lastManualLocationString";
    private static final String LAST_MANUAL_LOCATION = "lastManualLocation";
    private static final String WEATHER_DATA = "weatherData";
    private static final String LAST_UPDATE = "lastUpdate";
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 0;
    private static final int REQUEST_CODE_LOCATION_SETTINGS = 0;

    private Menu menu;
    private SharedPreferences preferences;
    private GoogleApiClient googleApiClient;

    private boolean recreated;
    private Location lastLocation;
    private String lastManualLocationString;
    private Location lastManualLocation;
    private WeatherData weatherData;
    private Calendar lastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        menu = null;
        loadPreferences();

        if (savedInstanceState != null) {
            recreated = true;
            lastLocation = savedInstanceState.getParcelable(LAST_LOCATION);
            lastManualLocationString = (String) savedInstanceState.getSerializable(LAST_MANUAL_LOCATION_STRING);
            lastManualLocation = savedInstanceState.getParcelable(LAST_MANUAL_LOCATION);
            weatherData = (WeatherData) savedInstanceState.getSerializable(WEATHER_DATA);
            lastUpdate = (Calendar) savedInstanceState.getSerializable(LAST_UPDATE);
        }
        else {
            recreated = false;
            lastLocation = null;
            lastManualLocationString = "";
            lastManualLocation = null;
            weatherData = null;
            lastUpdate = null;

            loadData();
        }

        updateUI();

        googleApiClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).addApi(LocationServices.API).
                build();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelable(LAST_LOCATION, lastLocation);
        savedInstanceState.putSerializable(LAST_MANUAL_LOCATION_STRING, lastManualLocationString);
        savedInstanceState.putParcelable(LAST_MANUAL_LOCATION, lastManualLocation);
        savedInstanceState.putSerializable(WEATHER_DATA, weatherData);
        savedInstanceState.putSerializable(LAST_UPDATE, lastUpdate);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart(){
        googleApiClient.connect();

        super.onStart();
    }

    @Override
    protected void onStop(){
        googleApiClient.disconnect();

        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();

        loadPreferences();
    }

    @Override
    protected void onPause(){
        super.onPause();

        stopLocationUpdates();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_update:
                update(true);
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint){
        if (!recreated) {
            update(true);
        }
    }

    @Override
    public void onConnectionSuspended(int cause){
        Log.d(TAG,"Google API connection suspended: " + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result){
        Log.d(TAG,"Google API connection failed: " + result.getErrorCode());

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        googleApiAvailability.getErrorDialog(this,
                googleApiAvailability.isGooglePlayServicesAvailable(this), 0).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    update(true);
                }
                else{
                    Log.d(TAG, "ACCESS_FINE_LOCATION permission not granted");
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_LOCATION_SETTINGS:
                if (resultCode == RESULT_OK) {
                    // Location settings have been modified as needed,
                    // so we will try to update again
                    update(true);
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        // We received a location finally, so we should be able to stop listening for them
        stopLocationUpdates();

        update(false);
    }

    protected void stopLocationUpdates(){
        if (googleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void loadPreferences(){
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);

            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        }
    }

    private void saveData(){
        StorageHelper.saveToInternal(this, WEATHER_DATA, weatherData);
        StorageHelper.saveToInternal(this, LAST_UPDATE, lastUpdate);
        StorageHelper.saveToInternal(this, LAST_MANUAL_LOCATION_STRING, lastManualLocationString);
        // We save as a SimpleLocation because Location is not serializable
        if (lastManualLocation != null) {
            StorageHelper.saveToInternal(this, LAST_MANUAL_LOCATION, new SimpleLocation(lastManualLocation));
        }
    }

    private void loadData(){
        weatherData = (WeatherData) StorageHelper.loadFromInternal(this, WEATHER_DATA);
        lastUpdate = (Calendar) StorageHelper.loadFromInternal(this, LAST_UPDATE);
        lastManualLocationString = (String) StorageHelper.loadFromInternal(this, LAST_MANUAL_LOCATION_STRING);
        SimpleLocation simpleLocation = (SimpleLocation) StorageHelper.loadFromInternal(this, LAST_MANUAL_LOCATION);
        if (simpleLocation != null) {
            lastManualLocation = simpleLocation.toLocation();
        }
    }

    private void updateManualLocation(){
        try {
            JSONAsyncRequest asyncRequest = new JSONAsyncRequest();
            asyncRequest.setType(JSONAsyncRequest.Type.GET_COORDINATES);
            asyncRequest.setLocationString(preferences.getString(SettingsActivity.KEY_PREF_EDITTEXT_MANUAL_LOCATION, ""));
            asyncRequest.setCallBack(this);
            JSONAsyncTask task = new JSONAsyncTask(asyncRequest);
            task.execute();
        } catch (Exception e) {
            Log.w(TAG, "Error in updateManualLocation", e);

            Toast.makeText(getApplicationContext(),"Failed to determine coordinates",Toast.LENGTH_SHORT).show();
        }
    }

    private void update(boolean updateLastLocation){
        if (preferences.getBoolean(SettingsActivity.KEY_PREF_CHECKBOX_LOCATION, false)) {
            updateLastLocation = false;

            // If the last manual location string is not empty and equals the configured manual location,
            // the manual location should be usable
            if (lastManualLocationString != null && !lastManualLocationString.isEmpty() &&
                    lastManualLocationString.equals(preferences.getString(SettingsActivity.KEY_PREF_EDITTEXT_MANUAL_LOCATION, ""))) {
                lastLocation = lastManualLocation;
            }
            else{
                updateManualLocation();

                return;
            }
        }

        // Do we want to try to update the location?
        if (updateLastLocation) {
            // Is the Google API client connected?
            if (googleApiClient != null && googleApiClient.isConnected()) {
                // Do we have permission to access locations?
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Attempt to retrieve the last device location
                    final Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                    if (location != null) {
                        lastLocation = location;
                    }
                    else{
                        Log.w(TAG, "getLastLocation returned null");

                        // Since getLastLocation returned null, we will try forcing the issue
                        // by requesting location updates
                        LocationRequest locationRequest = new LocationRequest();
                        locationRequest.setInterval(1000);
                        locationRequest.setFastestInterval(100);
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                        // But first we check to see if we can access locations
                        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                            @Override
                            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                                switch (locationSettingsResult.getStatus().getStatusCode()) {
                                    case LocationSettingsStatusCodes.SUCCESS:
                                        // We can request locations, so we will try that
                                        break;
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        try {
                                            locationSettingsResult.getStatus().startResolutionForResult(MainActivity.this,
                                                    REQUEST_CODE_LOCATION_SETTINGS);
                                        } catch (IntentSender.SendIntentException e){
                                            Log.e(TAG, "Error sending intent for location settings");
                                        }
                                        break;
                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        // We cannot request locations,
                                        // and we cannot fix the settings
                                        Log.d(TAG, "Cannot request location updates");
                                        break;
                                }
                            }
                        });

                        Log.d(TAG, "Requesting location updates");

                        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

                        return;
                    }
                }
                else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_ACCESS_FINE_LOCATION);
                }
            }
            else {
                Log.d(TAG, "Not connected to Google Play services");
            }
        }

        if (lastLocation != null) {
            try {
                JSONAsyncRequest asyncRequest = new JSONAsyncRequest();
                asyncRequest.setType(JSONAsyncRequest.Type.GET_WEATHER_DATA);
                asyncRequest.setLocation(lastLocation);
                asyncRequest.setCallBack(this);
                JSONAsyncTask task = new JSONAsyncTask(asyncRequest);
                task.execute();
            } catch (Exception e) {
                Log.w(TAG, "Error in update", e);

                Toast.makeText(getApplicationContext(),"Failed to connect to server",Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Failed to determine location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void taskStarting(JSONAsyncTask task){
        // Begin animating the refresh button
        if (menu != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ProgressBar progressBar = (ProgressBar) inflater.inflate(R.layout.progress_refresh, null);
            MenuItem item = menu.findItem(R.id.action_update);
            item.setActionView(progressBar);
        }
    }

    @Override
    public void taskComplete(JSONAsyncTask task){
        // Stop animating the refresh button
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_update);
            item.setActionView(null);
        }

        Exception e = task.getE();
        JSONAsyncRequest.Type type = task.getType();

        if (e != null) {
            Log.w(TAG, "Error updating for " + type, e);

            if (type == JSONAsyncRequest.Type.GET_COORDINATES) {
                Toast.makeText(getApplicationContext(), "Failed to determine coordinates", Toast.LENGTH_SHORT).show();
            }
            else if (type == JSONAsyncRequest.Type.GET_WEATHER_DATA) {
                Toast.makeText(getApplicationContext(), "Failed to connect to server", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            JSONAsyncRequest request = task.getRequest();
            JSONAsyncResult result = task.getResult();

            if (type == JSONAsyncRequest.Type.GET_COORDINATES) {
                Location locationResult = result.getLocation();

                Log.d(TAG, "Got coordinates result");

                if (locationResult != null) {
                    Log.d(TAG, "Result is valid");

                    lastManualLocationString = request.getLocationString();
                    lastManualLocation = locationResult;

                    saveData();

                    update(false);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to determine coordinates", Toast.LENGTH_SHORT).show();
                }
            }
            else if (type == JSONAsyncRequest.Type.GET_WEATHER_DATA) {
                WeatherData weatherResult = result.getWeatherData();

                Log.d(TAG, "Got weather data result");

                if (weatherResult.isValid()) {
                    Log.d(TAG, "Result is valid");

                    weatherData = weatherResult;

                    lastUpdate = Calendar.getInstance();

                    saveData();

                    updateUI();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to connect to server", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private int getWeatherImageID(String imageName){
        if (imageName.length() > 0) {
            int id = getResources().getIdentifier("medium_" + imageName, "drawable", getPackageName());

            if (id == 0) {
                // The medium version of the image doesn't exist, so try to use the large version
                id = getResources().getIdentifier("large_" + imageName, "drawable", getPackageName());
            }

            if (id == 0) {
                Log.w(TAG, "Failed to get image ID for " + imageName);
            }

            return id;
        }
        else{
            return 0;
        }
    }

    private boolean setImage(ImageView imageView, String imageName, WeatherDualImage dualImage) {
        if (dualImage != null) {
            int leftImageID = getWeatherImageID(dualImage.getLeftBaseName());
            int rightImageID = getWeatherImageID(dualImage.getRightBaseName());
            int dividerID = getResources().getIdentifier("divider", "drawable", getPackageName());

            if (leftImageID != 0 && rightImageID != 0 && dividerID != 0) {
                Bitmap leftImage = BitmapFactory.decodeResource(getResources(), leftImageID);
                Bitmap rightImage = BitmapFactory.decodeResource(getResources(), rightImageID);
                Bitmap divider = BitmapFactory.decodeResource(getResources(), dividerID);

                if (leftImage.getWidth() == rightImage.getWidth() &&
                        leftImage.getHeight() == rightImage.getHeight()) {
                    Bitmap combinedImage = Bitmap.createBitmap(leftImage.getWidth(), leftImage.getHeight(), Bitmap.Config.ARGB_8888);

                    Canvas combinedCanvas = new Canvas(combinedImage);

                    combinedCanvas.drawBitmap(leftImage, 0, 0, null);
                    combinedCanvas.drawBitmap(rightImage, leftImage.getWidth() / 2, 0, null);

                    if (dualImage.getLeftSuffix().length() > 0) {
                        int overlayID = getResources().getIdentifier("precip_chance_" + dualImage.getLeftSuffix(), "drawable", getPackageName());
                        if (overlayID != 0) {
                            Bitmap leftOverlay = BitmapFactory.decodeResource(getResources(), overlayID);

                            combinedCanvas.drawBitmap(leftOverlay,
                                    new Rect(leftOverlay.getWidth() / 2, 0, leftOverlay.getWidth(), leftOverlay.getHeight()),
                                    new Rect(0, 0, combinedImage.getWidth() / 2, combinedImage.getHeight()), null);
                        }
                    }

                    if (dualImage.getRightSuffix().length() > 0) {
                        int overlayID = getResources().getIdentifier("precip_chance_" + dualImage.getRightSuffix(), "drawable", getPackageName());
                        if (overlayID != 0) {
                            Bitmap rightOverlay = BitmapFactory.decodeResource(getResources(), overlayID);

                            combinedCanvas.drawBitmap(rightOverlay,
                                    new Rect(rightOverlay.getWidth() / 2, 0, rightOverlay.getWidth(), rightOverlay.getHeight()),
                                    new Rect(combinedImage.getWidth() / 2, 0, combinedImage.getWidth(), combinedImage.getHeight()), null);
                        }
                    }

                    combinedCanvas.drawBitmap(divider, 0, 0, null);

                    imageView.setImageBitmap(combinedImage);

                    return true;
                }
            }
        } else {
            int imageID = getWeatherImageID(imageName);

            if (imageID != 0) {
                imageView.setImageResource(imageID);

                return true;
            }
        }

        return false;
    }

    private void updateUI(){
        if(weatherData != null && weatherData.isValid()){
            weatherData.setUnits(preferences.getString(SettingsActivity.KEY_PREF_LIST_UNITS, ""));

            TextView textLastUpdate = (TextView)findViewById(R.id.textLastUpdate);
            RelativeLayout containerCurrentConditions = (RelativeLayout)findViewById(R.id.containerCurrentConditions);
            RelativeLayout containerForecast = (RelativeLayout)findViewById(R.id.containerForecast);
            RelativeLayout containerDetailedForecast = (RelativeLayout)findViewById(R.id.containerDetailedForecast);
            RelativeLayout containerHazards = (RelativeLayout)findViewById(R.id.containerHazards);

            textLastUpdate.setVisibility(View.VISIBLE);
            containerCurrentConditions.setVisibility(View.VISIBLE);

            TextView textSource = (TextView)findViewById(R.id.textSource);
            TextView textCurrentWeather = (TextView)findViewById(R.id.textCurrentWeather);
            ImageView imageCurrentWeather = (ImageView) findViewById(R.id.imageCurrentWeather);
            TextView textCurrentConditions = (TextView)findViewById(R.id.textCurrentConditions);

            SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance();
            textLastUpdate.setText("Last refreshed: " + simpleDateFormat.format(lastUpdate.getTime()));

            textSource.setText(Html.fromHtml("Current conditions at<br><b><font color=\"" +
                    String.format("#%06x", (0xFFFFFF & ContextCompat.getColor(this, R.color.colorPrimaryDark))) +
                    "\">" + weatherData.getFormattedSource() + "</font></b><br>" + weatherData.getFormattedPosition()));

            textCurrentWeather.setText(Html.fromHtml(weatherData.getFormattedCurrentWeather() + "<br><b>" + weatherData.getFormattedCurrentTemperature() + "</b>"));

            if (!setImage(imageCurrentWeather, weatherData.getFormattedCurrentWeatherImage(), weatherData.getCurrentWeatherDualImage())) {
                RelativeLayout.LayoutParams paramsCurrentWeather = (RelativeLayout.LayoutParams)textCurrentWeather.getLayoutParams();
                paramsCurrentWeather.addRule(RelativeLayout.BELOW, R.id.textSource);
                paramsCurrentWeather.addRule(RelativeLayout.ALIGN_LEFT, R.id.textSource);
                textCurrentWeather.setLayoutParams(paramsCurrentWeather);

                RelativeLayout.LayoutParams paramsCurrentConditions = (RelativeLayout.LayoutParams)textCurrentConditions.getLayoutParams();
                paramsCurrentConditions.addRule(RelativeLayout.BELOW, R.id.textCurrentWeather);
                paramsCurrentConditions.addRule(RelativeLayout.ALIGN_LEFT, R.id.textCurrentWeather);
                textCurrentConditions.setLayoutParams(paramsCurrentConditions);
            }

            String conditions = weatherData.getFormattedRelativeHumidity() + "\n" + weatherData.getFormattedWind() +
                    "\n" + weatherData.getFormattedAtmosphericPressure() + "\n" + weatherData.getFormattedDewPoint() +
                    "\n" + weatherData.getFormattedApparentTemperature() + "\n" + weatherData.getFormattedVisibility() +
                    "\n" + weatherData.getFormattedCreationDate();

            // Remove any duplicate newlines
            conditions = conditions.replaceAll("(\\n)\\1+","$1");
            // Replace newlines with '<br>'
            conditions = conditions.replaceAll("(\\n)","<br>");

            textCurrentConditions.setText(Html.fromHtml(conditions));

            LinearLayout forecastScroller = (LinearLayout)findViewById(R.id.forecastScroller);
            LinearLayout detailedForecastLayout = (LinearLayout)findViewById(R.id.detailedForecastLayout);

            forecastScroller.removeAllViews();
            detailedForecastLayout.removeAllViews();

            WeatherPeriod[] periods = weatherData.getPeriods();
            if(periods != null){
                containerForecast.setVisibility(View.VISIBLE);
                containerDetailedForecast.setVisibility(View.VISIBLE);

                int i = 0;
                for (WeatherPeriod period : periods) {
                    LayoutInflater inflater = getLayoutInflater();

                    // Setup the forecast scroller
                    LinearLayout viewForecast = (LinearLayout) inflater.inflate(R.layout.forecast_scroller_item, null);
                    TextView forecastScrollerTitle = (TextView) viewForecast.findViewById(R.id.forecastScrollerTitle);
                    ImageView forecastScrollerImage = (ImageView) viewForecast.findViewById(R.id.forecastScrollerImage);
                    TextView forecastScrollerText = (TextView) viewForecast.findViewById(R.id.forecastScrollerText);
                    forecastScrollerTitle.setText(Html.fromHtml("<b>" + period.getFormattedName() + "</b>"));
                    forecastScrollerText.setText(Html.fromHtml(period.getFormattedWeather() + "<br><b>" + period.getFormattedTemperatureLabel() + ": " + period.getFormattedTemperature() + "</b>"));

                    setImage(forecastScrollerImage, period.getFormattedWeatherImage(), period.getWeatherDualImage());

                    if(i % 2 == 0){
                        viewForecast.setBackgroundColor(ContextCompat.getColor(this, R.color.forecastItemBackground));
                    }
                    forecastScroller.addView(viewForecast);

                    // Setup the detailed forecast
                    TextView viewDetailedForecast = (TextView)inflater.inflate(R.layout.detailed_forecast_item, null);
                    viewDetailedForecast.setText(Html.fromHtml("<b>" + period.getFormattedName() + ":</b> " + period.getFormattedText()));
                    if(i % 2 == 0){
                        viewDetailedForecast.setBackgroundColor(ContextCompat.getColor(this, R.color.forecastItemBackground));
                    }
                    detailedForecastLayout.addView(viewDetailedForecast);

                    i++;
                }
            }
            else{
                containerForecast.setVisibility(View.GONE);
                containerDetailedForecast.setVisibility(View.GONE);
            }

            LinearLayout hazardsLayout = (LinearLayout)findViewById(R.id.hazardsLayout);

            hazardsLayout.removeAllViews();

            WeatherHazard[] hazards = weatherData.getHazards();
            if(hazards != null){
                containerHazards.setVisibility(View.VISIBLE);

                for (final WeatherHazard hazard : hazards) {
                    LayoutInflater inflater = getLayoutInflater();

                    TextView viewHazard = (TextView)inflater.inflate(R.layout.hazard_item, null);
                    viewHazard.setText(hazard.getFormattedText());

                    Linkify.TransformFilter filter = new Linkify.TransformFilter() {
                        @Override
                        public String transformUrl(Matcher matcher, String s) {
                            return hazard.getUrl();
                        }
                    };
                    Linkify.addLinks(viewHazard,Pattern.compile(hazard.getFormattedText()), null, null, filter);

                    hazardsLayout.addView(viewHazard);
                }
            }
            else{
                containerHazards.setVisibility(View.GONE);
            }
        }
    }
}
