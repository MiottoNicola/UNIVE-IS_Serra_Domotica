package com.example.SerraDomotica;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GreenhousesActivity extends BaseActivity {
    private GreenhouseAdapter adapter;
    private List<String> greenhouseList;
    private TextView noGreenhousesMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greenhouses);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setSubtitle(null);
        }
        toolbar.setTitle(getString(R.string.app_name));

        ImageView addIcon = findViewById(R.id.addIcon);
        addIcon.setOnClickListener(v -> showAddGreenhouseDialog());

        noGreenhousesMessage = findViewById(R.id.no_greenhouses_message);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewGreenhouses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        greenhouseList = new ArrayList<>();
        adapter = new GreenhouseAdapter(greenhouseList, this);
        recyclerView.setAdapter(adapter);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            fetchGreenhouses(currentUser.getUid());
        }else{
            Toast.makeText(this, getString(R.string.notAuthenticatedUser_toastText), Toast.LENGTH_SHORT).show();
        }

    }

    private void fetchGreenhouses(String userId) {
        DatabaseReference userDevicesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("devices");
        userDevicesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    noGreenhousesMessage.setVisibility(View.GONE);
                    greenhouseList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String greenhouseId = snapshot.getKey();
                        if (greenhouseId != null) {
                            fetchGreenhouseTitle(greenhouseId);
                        }
                    }
                }else{
                    noGreenhousesMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GreenhousesActivity.this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(GreenhousesActivity.this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
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

    // ProfileActivity.java
    private void showAddGreenhouseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.addDevice_imageDescription));

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_greenhouse, null);
        builder.setView(dialogView);

        final EditText input = dialogView.findViewById(R.id.editTextGreenhouseId);
        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialogInterface, which) -> {
            String greenhouseId = input.getText().toString();
            addGreenhouse(greenhouseId, GreenhousesActivity.this);
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel_buttonText), (dialogInterface, which) -> dialog.dismiss());

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black, null));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black, null));
        });

        dialog.show();
    }

}