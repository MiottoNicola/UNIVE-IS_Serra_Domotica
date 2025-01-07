package com.example.SerraDomotica;

import android.app.Dialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HistoryChartActivity extends BaseActivity {
    private LineChart lineChartTemperature;
    private LineChart lineChartAirHumidity;
    private LineChart lineChartSoilHumidity;
    private LineChart lineChartLuminosity;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_chart);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String idDevice = getIntent().getStringExtra("idDevice");

        ImageView legend = findViewById(R.id.toolbar_info);
        legend.setOnClickListener(v -> showTermsConditionsDialog());


        lineChartTemperature = findViewById(R.id.lineChartTemperature);
        lineChartAirHumidity = findViewById(R.id.lineChartAirHumidity);
        lineChartSoilHumidity = findViewById(R.id.lineChartSoilHumidity);
        lineChartLuminosity = findViewById(R.id.lineChartLuminosity);

        databaseReference = FirebaseDatabase.getInstance().getReference("/devices/" + idDevice + "/history");
        getGreenhouseName(idDevice, greenhouseName -> {
            if (greenhouseName != null) {
                getSupportActionBar().setTitle(greenhouseName + " - "+getString(R.string.history_activityTitle));
            } else {
                getSupportActionBar().setTitle(getString(R.string.greenhouseDetails_activityTitle));
            }
        });
        fetchDataFromDatabase();
    }

    private void showTermsConditionsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_history_legend);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        TextView closeButton = dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void fetchDataFromDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Entry> temperatureEntries = new ArrayList<>();
                List<Entry> airHumidityEntries = new ArrayList<>();
                List<Entry> soilHumidityEntries = new ArrayList<>();
                List<Entry> luminosityEntries = new ArrayList<>();

                int index = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    float airTemperature = snapshot.child("air_temperature").getValue(Float.class);
                    float airHumidity = snapshot.child("air_humidity").getValue(Float.class);
                    float soilHumidity = snapshot.child("soil_humidity").getValue(Float.class);
                    float luminosity = snapshot.child("luminosity").getValue(Float.class);

                    temperatureEntries.add(new Entry(index, airTemperature));
                    airHumidityEntries.add(new Entry(index, airHumidity));
                    soilHumidityEntries.add(new Entry(index, soilHumidity));
                    luminosityEntries.add(new Entry(index, luminosity));
                    index++;
                }

                setupChart(lineChartTemperature, temperatureEntries, "Temperature");
                setupChart(lineChartAirHumidity, airHumidityEntries, "Air Humidity");
                setupChart(lineChartSoilHumidity, soilHumidityEntries, "Soil Humidity");
                setupChart(lineChartLuminosity, luminosityEntries, "Luminosity");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HistoryChartActivity.this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupChart(LineChart chart, List<Entry> entries, String label) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getDescription().setText(getString(R.string.historyChart_labelDescription));

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.invalidate();
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