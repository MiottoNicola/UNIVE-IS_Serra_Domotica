package com.example.SerraDomotica;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GreenhousesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GreenhouseAdapter adapter;
    private List<String> greenhouseList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ImageView addIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greenhouses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Your Greenhouses");

        addIcon = findViewById(R.id.addIcon);

        addIcon.setOnClickListener(v -> showAddGreenhouseDialog());

        recyclerView = findViewById(R.id.recyclerViewGreenhouses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        greenhouseList = new ArrayList<>();
        adapter = new GreenhouseAdapter(greenhouseList, this);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            fetchGreenhouses(currentUser.getUid());
        }else{
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }

    }

    private void fetchGreenhouses(String userId) {
        DatabaseReference userDevicesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("devices");
        userDevicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                greenhouseList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String greenhouseId = snapshot.getKey();
                    if (greenhouseId != null) {
                        fetchGreenhouseTitle(greenhouseId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GreenhousesActivity.this, "Failed to load greenhouses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGreenhouseTitle(String greenhouseId) {
        DatabaseReference greenhouseRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId);
        greenhouseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String greenhouseName = dataSnapshot.child("titolo").getValue(String.class);
                if (greenhouseName != null) {
                    greenhouseList.add(greenhouseName + ":" + greenhouseId);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GreenhousesActivity.this, "Failed to load greenhouse title", Toast.LENGTH_SHORT).show();
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

    private void onGreenhouseClick(String greenhouseName) {
        Intent intent = new Intent(this, GreenhouseDetailsActivity.class);
        intent.putExtra("greenhouse_name", greenhouseName);
        startActivity(intent);
    }

    private void showAddGreenhouseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Greenhouse");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String greenhouseName = input.getText().toString();
                addGreenhouse(greenhouseName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void addGreenhouse(String greenhouseId) {
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId);
        deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isConnected = dataSnapshot.child("config").child("isConnected").getValue(Boolean.class);
                    if (isConnected != null && !isConnected) {
                        // Update isConnected to true
                        deviceRef.child("config").child("isConnected").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Add the greenhouse ID to the user's devices
                                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    DatabaseReference userDevicesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("devices").child(greenhouseId);
                                    userDevicesRef.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(GreenhousesActivity.this, "Greenhouse added successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(GreenhousesActivity.this, "Failed to add greenhouse to user devices", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Toast.makeText(GreenhousesActivity.this, "Failed to update greenhouse config", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(GreenhousesActivity.this, "Greenhouse is already connected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GreenhousesActivity.this, "Greenhouse ID does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GreenhousesActivity.this, "Failed to check greenhouse ID", Toast.LENGTH_SHORT).show();
            }
        });
    }
}