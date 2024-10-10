package com.example.SerraDomotica;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GreenhousesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GreenhouseAdapter adapter;
    private List<String> greenhouseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greenhouses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Your Greenhouses");

        recyclerView = findViewById(R.id.recyclerViewGreenhouses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        greenhouseList = new ArrayList<>();
        greenhouseList.add("Greenhouse 1");
        greenhouseList.add("Greenhouse 2");
        greenhouseList.add("Greenhouse 3");

        adapter = new GreenhouseAdapter(greenhouseList, this::onGreenhouseClick);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onGreenhouseClick(String greenhouseName) {
        Intent intent = new Intent(this, GreenhouseDetailsActivity.class);
        intent.putExtra("greenhouse_name", greenhouseName);
        startActivity(intent);
    }
}