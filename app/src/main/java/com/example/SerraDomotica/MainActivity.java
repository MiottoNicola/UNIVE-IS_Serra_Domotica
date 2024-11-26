package com.example.SerraDomotica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.SerraDomotica.forecast.Forecast;
import com.example.SerraDomotica.forecast.ForecastRepository;
import com.example.SerraDomotica.utils.FirebaseUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String TAG = "MainActivity";

    private TextView textLocation, textDescription, textTemperature, textHumidity, textWind;
    private ImageView weatherIcon, ic_profile;
    private Button buttonGreenhouses;
    private FusedLocationProviderClient fusedLocationClient;
    private ForecastRepository forecastRepository = new ForecastRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        FirebaseUtils.checkUserLoggedIn(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Trova gli elementi della card
        textLocation = findViewById(R.id.textLocation);
        textDescription = findViewById(R.id.textDescription);
        textTemperature = findViewById(R.id.textTemperature);
        textHumidity = findViewById(R.id.textHumidity);
        textWind = findViewById(R.id.textWind);
        weatherIcon = findViewById(R.id.weatherIcon);
        buttonGreenhouses = findViewById(R.id.buttonGreenhouses);

        // Inizializza il client per ottenere la posizione
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            getLastLocation();
        }

        ic_profile = findViewById(R.id.profileIcon);

        ic_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        buttonGreenhouses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GreenhousesActivity.class);
                startActivity(intent);
            }
        });
    }



    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Log.d(TAG, "Location obtained: " + latitude + ", " + longitude);
                    fetchWeatherData(latitude, longitude);
                } else {
                    Log.d(TAG, "Failed to get location");
                }
            }
        });
    }

    private void fetchWeatherData(double latitude, double longitude) {
        Log.d(TAG, "Fetching weather data for: " + latitude + ", " + longitude);
        new Thread(() -> {
            try {
                Forecast forecast = forecastRepository.getForecastForLocation(latitude, longitude);
                Log.d(TAG, "Weather data obtained: " + forecast.toString());
                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.d(TAG, "Updating weather card with forecast: " + forecast.toString());
                    updateWeatherCard(forecast);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateWeatherCard(Forecast forecast) {
        Log.d(TAG, "Updating weather card");
        textLocation.setText("Lat: " + forecast.getLatitude() + ", Lon: " + forecast.getLongitude());
        textDescription.setText(forecast.getForecastDescription());
        textTemperature.setText(String.format("%.2f°C", forecast.getTemperature()));
        textHumidity.setText(forecast.getHumidity() + "%");
        textWind.setText(String.format("%.2f km/h", forecast.getWindSpeed()*3,6));

        Log.d(TAG, "Loading weather icon: " + Uri.parse(forecastRepository.getIconURL(forecast)));
        Picasso.get().load(Uri.parse(forecastRepository.getIconURL(forecast))).error(R.drawable.ic_launcher_foreground).into(weatherIcon);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }
}