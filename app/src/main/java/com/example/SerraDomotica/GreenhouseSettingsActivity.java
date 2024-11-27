package com.example.SerraDomotica;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class GreenhouseSettingsActivity extends BaseActivity {

    private EditText minAirTemp, maxAirTemp, minAirHumidity, maxAirHumidity, minSoilHumidity, maxSoilHumidity, minLuminosity, maxLuminosity, textName;
    private Button buttonSaveAirTemp, buttonSaveAirHumidity, buttonSaveSoilHumidity, buttonSaveLuminosity, buttonSaveName;
    private DatabaseReference configRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greenhouse_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String greenhouseId = getIntent().getStringExtra("greenhouse_id");
        String greenhouseName = getIntent().getStringExtra("greenhouse_name");
        getSupportActionBar().setTitle(greenhouseName + " - Settings");

        minAirTemp = findViewById(R.id.min_air_temperature);
        maxAirTemp = findViewById(R.id.max_air_temperature);
        minAirHumidity = findViewById(R.id.min_air_humidity);
        maxAirHumidity = findViewById(R.id.max_air_humidity);
        minSoilHumidity = findViewById(R.id.min_soil_humidity);
        maxSoilHumidity = findViewById(R.id.max_soil_humidity);
        minLuminosity = findViewById(R.id.min_luminosity);
        maxLuminosity = findViewById(R.id.max_luminosity);
        textName = findViewById(R.id.gname);

        buttonSaveAirTemp = findViewById(R.id.button_save_air_temperature);
        buttonSaveAirHumidity = findViewById(R.id.button_save_air_humidity);
        buttonSaveSoilHumidity = findViewById(R.id.button_save_soil_humidity);
        buttonSaveLuminosity = findViewById(R.id.button_save_luminosity);
        buttonSaveName = findViewById(R.id.button_save_name);

        if(greenhouseId == null) {
            Toast.makeText(this, "Greenhouse ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        configRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId).child("config");

        fetchDefaultValues();
        textName.setText(greenhouseName);

        findViewById(R.id.button_edit_air_temp).setOnClickListener(v -> enableEditing(minAirTemp, maxAirTemp, buttonSaveAirTemp));
        findViewById(R.id.button_edit_air_humidity).setOnClickListener(v -> enableEditing(minAirHumidity, maxAirHumidity, buttonSaveAirHumidity));
        findViewById(R.id.button_edit_soil_humidity).setOnClickListener(v -> enableEditing(minSoilHumidity, maxSoilHumidity, buttonSaveSoilHumidity));
        findViewById(R.id.button_edit_luminosity).setOnClickListener(v -> enableEditing(minLuminosity, maxLuminosity, buttonSaveLuminosity));
        findViewById(R.id.button_edit_name).setOnClickListener(v -> enableEditing(textName, textName, buttonSaveName));

        buttonSaveAirTemp.setOnClickListener(v -> saveSettings(minAirTemp, buttonSaveAirTemp,  maxAirTemp, "min_temp", "max_temp"));
        buttonSaveAirHumidity.setOnClickListener(v -> saveSettings(minAirHumidity, buttonSaveAirHumidity, maxAirHumidity, "min_air_hum", "max_air_hum"));
        buttonSaveSoilHumidity.setOnClickListener(v -> saveSettings(minSoilHumidity, buttonSaveSoilHumidity, maxSoilHumidity, "min_soil_hum", "max_soil_hum"));
        buttonSaveLuminosity.setOnClickListener(v -> saveSettings(minLuminosity, buttonSaveLuminosity, maxLuminosity, "min_lum", "max_lum"));
        buttonSaveName.setOnClickListener(v -> {
            String newName = textName.getText().toString();
            FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId).child("titolo").setValue(newName);
            disableEditing(textName, textName, buttonSaveName);
            Toast.makeText(this, "Name saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(GreenhouseSettingsActivity.this, GreenhousesActivity.class);
            startActivity(intent);
        });
    }

    private void fetchDefaultValues() {
        configRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    minAirTemp.setText(dataSnapshot.child("min_temp").getValue(Float.class).toString());
                    maxAirTemp.setText(dataSnapshot.child("max_temp").getValue(Float.class).toString());
                    minAirHumidity.setText(dataSnapshot.child("min_air_hum").getValue(Integer.class).toString());
                    maxAirHumidity.setText(dataSnapshot.child("max_air_hum").getValue(Integer.class).toString());
                    minSoilHumidity.setText(dataSnapshot.child("min_soil_hum").getValue(Integer.class).toString());
                    maxSoilHumidity.setText(dataSnapshot.child("max_soil_hum").getValue(Integer.class).toString());
                    minLuminosity.setText(dataSnapshot.child("min_lum").getValue(Integer.class).toString());
                    maxLuminosity.setText(dataSnapshot.child("max_lum").getValue(Integer.class).toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GreenhouseSettingsActivity.this, "Failed to load default values", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enableEditing(EditText minField, EditText maxField, Button saveButton) {
        minField.setEnabled(true);
        maxField.setEnabled(true);
        saveButton.setVisibility(View.VISIBLE);
    }

    private void disableEditing(EditText minField, EditText maxField, Button saveButton) {
        minField.setEnabled(false);
        maxField.setEnabled(false);
        saveButton.setVisibility(View.GONE);
    }

    private void saveSettings(EditText minField, Button saveButton, EditText maxField, String minKey, String maxKey) {
        try {
            double minValue = Double.parseDouble(minField.getText().toString());
            double maxValue = Double.parseDouble(maxField.getText().toString());

            if(minValue >= maxValue) {
                Toast.makeText(this, "Min value should be less than max value", Toast.LENGTH_SHORT).show();
                return;
            }

            configRef.child(minKey).setValue(minValue);
            configRef.child(maxKey).setValue(maxValue);

            disableEditing(minField, maxField, saveButton);
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}