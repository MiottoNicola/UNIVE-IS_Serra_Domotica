package com.example.SerraDomotica;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.SerraDomotica.forecast.Forecast;
import com.example.SerraDomotica.forecast.ForecastRepository;
import com.example.SerraDomotica.utils.FirebaseUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int LOCATION_UPDATE_INTERVAL = 10000; // 10 secondi
    private static final int LOCATION_FASTEST_INTERVAL = 5000; // 5 secondi

    private TextView textLocation, textDescription, textTemperature, textHumidity, textWind, textError;
    private ImageView weatherIcon, iconTemperature, iconHumidity, iconWind;
    private FusedLocationProviderClient fusedLocationClient;
    private final ForecastRepository forecastRepository = new ForecastRepository();
    private LocationCallback locationCallback;

    private final BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.d("GPS", "GPS enabled");
                    requestLocationUpdates();
                } else {
                    Log.d("GPS", "GPS disabled");
                    showErrorState();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        FirebaseUtils.checkUserLoggedIn(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textError = findViewById(R.id.textError);
        textLocation = findViewById(R.id.textLocation);
        textDescription = findViewById(R.id.textDescription);
        textTemperature = findViewById(R.id.textTemperature);
        textHumidity = findViewById(R.id.textHumidity);
        textWind = findViewById(R.id.textWind);
        weatherIcon = findViewById(R.id.weatherIcon);
        iconTemperature = findViewById(R.id.iconTemperature);
        iconHumidity = findViewById(R.id.iconHumidity);
        iconWind = findViewById(R.id.iconWind);
        Button buttonGreenhouses = findViewById(R.id.buttonGreenhouses);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        configureLocationCallback();
        checkPermissionsAndRequestUpdates();

        ImageView ic_profile = findViewById(R.id.profileIcon);
        ic_profile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        buttonGreenhouses.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GreenhousesActivity.class)));

        registerReceiver(gpsReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));
    }

    private void configureLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    fetchWeatherData(latitude, longitude); // Recupera i dati meteo con le coordinate
                }
            }
        };
    }

    private void checkPermissionsAndRequestUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            requestLocationUpdates();
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, // Priorità alta per GPS
                LOCATION_UPDATE_INTERVAL) // Intervallo di aggiornamento
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL) // Intervallo più veloce
                .setWaitForAccurateLocation(false) // Non aspetta una posizione super accurata
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    private void fetchWeatherData(double latitude, double longitude) {
        new Thread(() -> {
            try {
                if(!isNetConnected) {
                    return;
                }
                Forecast forecast = forecastRepository.getForecastForLocation(latitude, longitude);
                new Handler(Looper.getMainLooper()).post(() -> updateWeatherCard(forecast));
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_LONG).show();
            }
        }).start();
    }

    private void updateWeatherCard(Forecast forecast) {
        textError.setVisibility(View.INVISIBLE);
        iconHumidity.setVisibility(View.VISIBLE);
        iconTemperature.setVisibility(View.VISIBLE);
        iconWind.setVisibility(View.VISIBLE);
        weatherIcon.setVisibility(View.VISIBLE);
        textLocation.setText(String.format(Locale.getDefault(), "Lat: %s, Lon: %s", forecast.getLatitude(), forecast.getLongitude()));
        textDescription.setText(forecast.getForecastDescription());
        textTemperature.setText(String.format(Locale.getDefault(), "%.2f°C", forecast.getTemperature()));
        textHumidity.setText(String.format(Locale.getDefault(),"%d%%", forecast.getHumidity()));
        textWind.setText(String.format(Locale.getDefault(), "%.2f km/h", forecast.getWindSpeed() * 3.6));

        Picasso.get().load(Uri.parse(forecastRepository.getIconURL(forecast))).error(R.mipmap.ic_launcher_round).into(weatherIcon);
    }

    private void showErrorState() {
        textError.setVisibility(View.VISIBLE);
        iconHumidity.setVisibility(View.INVISIBLE);
        iconTemperature.setVisibility(View.INVISIBLE);
        iconWind.setVisibility(View.INVISIBLE);
        weatherIcon.setVisibility(View.INVISIBLE);
        textLocation.setText("");
        textDescription.setText("");
        textTemperature.setText("");
        textHumidity.setText("");
        textWind.setText("");
        Toast.makeText(this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionsAndRequestUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gpsReceiver);
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                showErrorState();
            }
        }
    }
}
