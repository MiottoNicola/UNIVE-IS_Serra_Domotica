package com.example.SerraDomotica;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ProfileActivity extends BaseActivity {

    private TextView textNomeCognome, textEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        textNomeCognome = findViewById(R.id.NomeCognome);
        textEmail = findViewById(R.id.Email);
        ImageView logoutIcon = findViewById(R.id.logoutIcon);
        LinearLayout deviceContainer = findViewById(R.id.device_container);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.profile_activityTitle));

        Button buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonResetPassword.setOnClickListener(v -> {
            if(currentUser == null) {
                Toast.makeText(ProfileActivity.this, getString(R.string.resetPasswordFailed_toastText), Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(Objects.requireNonNull(currentUser.getEmail()))
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, getString(R.string.resetPasswordSuccess_toastText), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, getString(R.string.resetPasswordFailed_toastText), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        ImageView buttonAddDevice = findViewById(R.id.buttonAddDevice);
        buttonAddDevice.setOnClickListener(v -> showAddGreenhouseDialog());

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            DatabaseReference databaseReferenceDevices = databaseReference.child("devices");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nomeCognome = dataSnapshot.child("nome_cognome").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        if (nomeCognome != null && email != null) {
                            textNomeCognome.setText(nomeCognome);
                            textEmail.setText(email);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
                }
            });

            databaseReferenceDevices.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Clear existing views
                        deviceContainer.removeAllViews();

                        // Add devices
                        for (DataSnapshot deviceSnapshot : dataSnapshot.getChildren()) {
                            String deviceId = deviceSnapshot.getKey();
                            if (deviceId == null) {
                                continue;
                            }
                            DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("titolo");

                            deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String deviceName = dataSnapshot.getValue(String.class);
                                        if (deviceName != null) {
                                            LinearLayout deviceItem = new LinearLayout(ProfileActivity.this);
                                            deviceItem.setOrientation(LinearLayout.HORIZONTAL);
                                            deviceItem.setGravity(Gravity.CENTER_VERTICAL);
                                            deviceItem.setPadding(0, 0, 0, 8);

                                            ImageView deviceIcon = new ImageView(ProfileActivity.this);
                                            deviceIcon.setImageResource(R.drawable.ic_device_hub);
                                            deviceItem.addView(deviceIcon);

                                            // Create TextView for device name
                                            TextView deviceNameView = new TextView(ProfileActivity.this);
                                            deviceNameView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                                            deviceNameView.setPadding(8, 8, 8, 8);
                                            deviceNameView.setText(deviceName);

                                            ImageView readMoreButton = new ImageView(ProfileActivity.this);
                                            readMoreButton.setImageResource(R.drawable.ic_read_more);
                                            readMoreButton.setOnClickListener(v -> {
                                                Intent intent = new Intent(ProfileActivity.this, GreenhouseDetailsActivity.class);
                                                intent.putExtra("greenhouse_name", deviceName);
                                                intent.putExtra("greenhouse_id", deviceId);
                                                startActivity(intent);
                                            });

                                            deviceItem.addView(deviceNameView);
                                            deviceItem.addView(readMoreButton);
                                            deviceContainer.addView(deviceItem);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    LinearLayout deviceItem = new LinearLayout(ProfileActivity.this);
                                    deviceItem.setOrientation(LinearLayout.HORIZONTAL);
                                    deviceItem.setGravity(Gravity.CENTER_VERTICAL);
                                    deviceItem.setPadding(0, 0, 0, 8);

                                    TextView noDevice = new TextView(ProfileActivity.this);
                                    noDevice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                                    noDevice.setPadding(8, 8, 8, 8);
                                    noDevice.setText(getString(R.string.noGreenhousesFound_text));

                                    deviceItem.addView(noDevice);
                                    deviceContainer.addView(deviceItem);
                                    Toast.makeText(ProfileActivity.this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    } else {
                        LinearLayout deviceItem = new LinearLayout(ProfileActivity.this);
                        deviceItem.setOrientation(LinearLayout.HORIZONTAL);
                        deviceItem.setGravity(Gravity.CENTER_VERTICAL);
                        deviceItem.setPadding(0, 0, 0, 8);

                        TextView noDevice = new TextView(ProfileActivity.this);
                        noDevice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                        noDevice.setPadding(8, 8, 8, 8);
                        noDevice.setText(getString(R.string.noGreenhousesFound_text));

                        deviceItem.addView(noDevice);
                        deviceContainer.addView(deviceItem);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
                }
            });
        }

        logoutIcon.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, getString(R.string.logoutSuccess_toastText), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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
            addGreenhouse(greenhouseId, ProfileActivity.this);
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel_buttonText), (dialogInterface, which) -> dialog.dismiss());

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black, null));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black, null));
        });

        dialog.show();
    }
}