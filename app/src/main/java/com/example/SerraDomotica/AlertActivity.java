package com.example.SerraDomotica;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlertActivity extends BaseActivity {
    private AlertAdapter alertAdapter;
    private List<String> alertList;
    private DatabaseReference alertRef;
    private TextView textViewNoAlerts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        String greenhouseId = getIntent().getStringExtra("greenhouse_id");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getGreenhouseName(greenhouseId, greenhouseName -> {
            if (greenhouseName != null) {
                getSupportActionBar().setTitle(greenhouseName + " - "+getString(R.string.alerts_activityTitle));
            } else {
                getSupportActionBar().setTitle(getString(R.string.greenhouseDetails_activityTitle));
            }
        });

        textViewNoAlerts = findViewById(R.id.text_view_no_alerts);
        RecyclerView recyclerView = findViewById(R.id.recycler_view_alerts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        alertList = new ArrayList<>();
        alertAdapter = new AlertAdapter(alertList);
        recyclerView.setAdapter(alertAdapter);

        if (greenhouseId == null) {
            Toast.makeText(this, getString(R.string.IDNotFound_toastText), Toast.LENGTH_SHORT).show();
        } else {
            alertRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId).child("alert");
            fetchAlerts();
        }
    }

    private void fetchAlerts() {
        alertRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                alertList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String alert = snapshot.getKey();
                    alertList.add(alert);
                }
                alertAdapter.notifyDataSetChanged();
                textViewNoAlerts.setVisibility(alertList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AlertActivity.this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
            }
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
}