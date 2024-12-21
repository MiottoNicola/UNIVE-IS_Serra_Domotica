package com.example.SerraDomotica;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WidgetConfigActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "com.example.SerraDomotica.WidgetProvider";
    public static final String PREF_PREFIX_KEY = "appwidget_";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ListView listView;
    private List<String> greenhouseNames = new ArrayList<>();
    private List<String> greenhouseIds = new ArrayList<>();
    private String selectedGreenhouseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_config);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Widget Configuration");
        }

        listView = findViewById(R.id.spinnerSerraList);
        Button buttonSave = findViewById(R.id.buttonSave);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load greenhouses from Firebase
        loadGreenhouses(currentUser.getUid());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedGreenhouseId = greenhouseIds.get(position);
        });

        buttonSave.setOnClickListener(v -> {
            if (selectedGreenhouseId != null) {
                saveWidgetConfiguration(this, appWidgetId, selectedGreenhouseId);
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);

                // Update the widget
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                WidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);

                finish();
            } else {
                Toast.makeText(this, "Please select a greenhouse", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGreenhouses(String userId) {
        DatabaseReference userDevicesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("devices");
        userDevicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String greenhouseId = snapshot.getKey();
                        Log.d("WidgetConfigActivity", "Greenhouse ID: " + greenhouseId);
                        DatabaseReference greenhouseRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId).child("titolo");
                        greenhouseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot greenhouseSnapshot) {
                                String greenhouseName = greenhouseSnapshot.getValue(String.class);
                                Log.d("WidgetConfigActivity", "Greenhouse Name: " + greenhouseName);
                                greenhouseIds.add(greenhouseId);
                                greenhouseNames.add(greenhouseName != null ? greenhouseName : "Unnamed Greenhouse");
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(WidgetConfigActivity.this, android.R.layout.simple_list_item_single_choice, greenhouseNames);
                                listView.setAdapter(adapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(WidgetConfigActivity.this, "Failed to load greenhouse names", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(WidgetConfigActivity.this, "No greenhouses found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(WidgetConfigActivity.this, "Failed to load greenhouses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveWidgetConfiguration(Context context, int appWidgetId, String greenhouseId) {
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(PREF_PREFIX_KEY + appWidgetId, greenhouseId)
                .apply();
    }
}