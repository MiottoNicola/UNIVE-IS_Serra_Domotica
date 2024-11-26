package com.example.SerraDomotica;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView textNomeCognome, textEmail;
    private ImageView logoutIcon, buttonAddDevice;
    private Button buttonResetPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceDevices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        textNomeCognome = findViewById(R.id.NomeCognome);
        textEmail = findViewById(R.id.Email);
        logoutIcon = findViewById(R.id.logoutIcon);
        LinearLayout deviceContainer = findViewById(R.id.device_container);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonResetPassword.setOnClickListener(v -> {
            mAuth.sendPasswordResetEmail(currentUser.getEmail())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        buttonAddDevice = findViewById(R.id.buttonAddDevice);
        buttonAddDevice.setOnClickListener(v -> showAddGreenhouseDialog());

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            databaseReferenceDevices = databaseReference.child("devices");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String nomeCognome = dataSnapshot.child("nome_cognome").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        if (nomeCognome != null && email != null) {
                            textNomeCognome.setText(nomeCognome);
                            textEmail.setText(email);
                        } else {
                            Toast.makeText(ProfileActivity.this, "NomeCognome or Email is null", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(ProfileActivity.this, "Profile does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile data.", Toast.LENGTH_SHORT).show();
                }
            });

            databaseReferenceDevices.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Clear existing views
                        deviceContainer.removeAllViews();

                        // Add devices
                        for (DataSnapshot deviceSnapshot : dataSnapshot.getChildren()) {
                            String deviceId = deviceSnapshot.getKey();
                            DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("devices").child(deviceId).child("titolo");

                            deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
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
                                public void onCancelled(DatabaseError databaseError) {
                                    LinearLayout deviceItem = new LinearLayout(ProfileActivity.this);
                                    deviceItem.setOrientation(LinearLayout.HORIZONTAL);
                                    deviceItem.setGravity(Gravity.CENTER_VERTICAL);
                                    deviceItem.setPadding(0, 0, 0, 8);

                                    TextView noDevice = new TextView(ProfileActivity.this);
                                    noDevice.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                                    noDevice.setPadding(8, 8, 8, 8);
                                    noDevice.setText("No device found");

                                    deviceItem.addView(noDevice);
                                    deviceContainer.addView(deviceItem);
                                    Toast.makeText(ProfileActivity.this, "Failed to load device data.", Toast.LENGTH_SHORT).show();
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
                        noDevice.setText("No device found");

                        deviceItem.addView(noDevice);
                        deviceContainer.addView(deviceItem);

                        Toast.makeText(ProfileActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to load devices.", Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            Toast.makeText(ProfileActivity.this, "User is null", Toast.LENGTH_SHORT).show();
            finish();
        }

        logoutIcon.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
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
                                    DatabaseReference userDevicesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("devices");
                                    userDevicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot userDevicesSnapshot) {
                                            if (!userDevicesSnapshot.exists()) {
                                                userDevicesRef.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            addGreenhouseToUserDevices(userDevicesRef, greenhouseId);
                                                        } else {
                                                            Toast.makeText(ProfileActivity.this, "Failed to add root /devices to user", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                addGreenhouseToUserDevices(userDevicesRef, greenhouseId);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(ProfileActivity.this, "Failed to check user devices", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Failed to update greenhouse config", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(ProfileActivity.this, "Greenhouse is already connected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Greenhouse ID does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to check greenhouse ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addGreenhouseToUserDevices(DatabaseReference userDevicesRef, String greenhouseId) {
        userDevicesRef.child(greenhouseId).setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Greenhouse added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to add greenhouse to user devices", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}