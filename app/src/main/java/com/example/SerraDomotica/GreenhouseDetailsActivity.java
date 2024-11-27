package com.example.SerraDomotica;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class GreenhouseDetailsActivity extends BaseActivity {
    private TextView textTemperatureValue, textHumidityValue, textLuminosityValue, textHumiditySoilValue, textDate;
    private SwitchCompat switchLight, switchWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greenhouse_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String greenhouseName1 = getIntent().getStringExtra("greenhouse_name");
        if (greenhouseName1 != null) {
            getSupportActionBar().setTitle(greenhouseName1);
        } else {
            getSupportActionBar().setTitle("Greenhouse Details");
        }

        ImageView delIcon = findViewById(R.id.delIcon);
        delIcon.setOnClickListener(v -> showDisconnectDialog());

        textTemperatureValue = findViewById(R.id.textTemperatureValue);
        textHumidityValue = findViewById(R.id.textHumidityValue);
        textLuminosityValue = findViewById(R.id.textLuminosityValue);
        textHumiditySoilValue = findViewById(R.id.textHumiditySoilValue);
        textDate = findViewById(R.id.textDate);

        switchLight = findViewById(R.id.switchLight);
        switchWater = findViewById(R.id.switchWater);

        String greenhouseId = getIntent().getStringExtra("greenhouse_id");
        String greenhouseName = getIntent().getStringExtra("greenhouse_name");
        if (greenhouseId != null && greenhouseName != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId);
            fetchGreenhouseDetails(greenhouseId);
            setupSwitchListeners(greenhouseId);
        } else {
            Toast.makeText(this, "No greenhouse ID or name provided", Toast.LENGTH_SHORT).show();
        }

        Button historyButton = findViewById(R.id.history_button);
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(GreenhouseDetailsActivity.this, TemperatureChartActivity.class);
            intent.putExtra("idDevice", greenhouseId);
            intent.putExtra("greenhouseName", greenhouseName);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(GreenhouseDetailsActivity.this, GreenhouseSettingsActivity.class);
            intent.putExtra("greenhouse_id", greenhouseId);
            intent.putExtra("greenhouse_name", greenhouseName);
            startActivity(intent);
        });

        Button alertButton = findViewById(R.id.alert_button);
        alertButton.setOnClickListener(v -> {
            Intent intent = new Intent(GreenhouseDetailsActivity.this, AlertActivity.class);
            intent.putExtra("greenhouse_id", greenhouseId);
            intent.putExtra("greenhouse_name", greenhouseName);
            startActivity(intent);
        });
    }

    private void fetchGreenhouseDetails(String greenhouseId) {
        String historyPath = "devices/" + greenhouseId + "/history";
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference(historyPath);

        historyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String dateKey = dateSnapshot.getKey();

                    Float airTemperature = dateSnapshot.child("air_temperature").getValue(Float.class);
                    Integer airHumidity = dateSnapshot.child("air_humidity").getValue(Integer.class);
                    Integer luminosity = dateSnapshot.child("luminosity").getValue(Integer.class);
                    Integer soilHumidity = dateSnapshot.child("soil_humidity").getValue(Integer.class);

                    if(dateKey != null) {
                        dateKey = dateKey.replace("_", ", ");
                        dateKey = dateKey.replace("-", "/");
                        textDate.setText("Last update: "+dateKey);
                    }

                    if (airTemperature != null) {
                        textTemperatureValue.setText(airTemperature.toString()+"°C");
                    }
                    if (airHumidity != null) {
                        textHumidityValue.setText(airHumidity.toString()+"%");
                    }
                    if (luminosity != null) {
                        textLuminosityValue.setText(luminosity.toString()+"%");
                    }
                    if (soilHumidity != null) {
                        textHumiditySoilValue.setText(soilHumidity.toString()+"%");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GreenhouseDetailsActivity.this, "Failed to load greenhouse details", Toast.LENGTH_SHORT).show();
            }
        });

        String configPath = "devices/" + greenhouseId + "/config";
        DatabaseReference configRef = FirebaseDatabase.getInstance().getReference(configPath);

        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean light = dataSnapshot.child("light").getValue(Boolean.class);
                Boolean water = dataSnapshot.child("water").getValue(Boolean.class);

                if (light != null) {
                    switchLight.setChecked(light);
                }
                if (water != null) {
                    switchWater.setChecked(water);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GreenhouseDetailsActivity.this, "Failed to load greenhouse config", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSwitchListeners(String greenhouseId) {
        switchLight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DatabaseReference lightRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId).child("config").child("light");
            lightRef.setValue(isChecked);
        });

        switchWater.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DatabaseReference waterRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId).child("config").child("water");
            waterRef.setValue(isChecked);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDisconnectDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Disconnect Device")
                .setMessage("Are you sure you want to disconnect the device?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // Handle the disconnect action
                    disconnectDevice();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void disconnectDevice() {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String greenhouseId = getIntent().getStringExtra("greenhouse_id");

        if(greenhouseId == null) {
            Toast.makeText(this, "No greenhouse ID found", Toast.LENGTH_SHORT).show();
            return;
        }
        // Remove the greenhouse ID from the user's devices
        DatabaseReference userDevicesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("devices").child(greenhouseId);
        userDevicesRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Set isConnected to false in the greenhouse configuration
                DatabaseReference greenhouseConfigRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId).child("config").child("isConnected");
                greenhouseConfigRef.setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(GreenhouseDetailsActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(GreenhouseDetailsActivity.this, GreenhousesActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(GreenhouseDetailsActivity.this, "Failed to update greenhouse config", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(GreenhouseDetailsActivity.this, "Failed to remove device from user", Toast.LENGTH_SHORT).show();
            }
        });
    }}