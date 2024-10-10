package com.example.SerraDomotica;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private TextView textNomeCognome, textEmail;
    private Button buttonResetPassword;
    private ImageView logoutIcon;
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
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        logoutIcon = findViewById(R.id.logoutIcon);
        LinearLayout deviceContainer = findViewById(R.id.device_container);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
            databaseReferenceDevices = FirebaseDatabase.getInstance().getReference("devices");

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
                            String deviceName = deviceSnapshot.child("titolo").getValue(String.class);

                            if (deviceName != null) {
                                // Create a new LinearLayout for each device
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

                                // Create Button for read more
                                ImageView readMoreButton = new ImageView(ProfileActivity.this);
                                readMoreButton.setImageResource(R.drawable.ic_read_more);
                                readMoreButton.setOnClickListener(v -> {
                                    // Handle read more click
                                    Toast.makeText(ProfileActivity.this, "Read more about " + deviceName, Toast.LENGTH_SHORT).show();
                                });

                                // Add views to deviceItem
                                deviceItem.addView(deviceNameView);
                                deviceItem.addView(readMoreButton);

                                // Add deviceItem to deviceContainer
                                deviceContainer.addView(deviceItem);
                            }
                        }
                    } else {
                        Toast.makeText(ProfileActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ProfileActivity.this, "Failed to load devices.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = textEmail.getText().toString();
                if (!email.isEmpty()) {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Failed to send reset email", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(ProfileActivity.this, "Email is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Toast.makeText(ProfileActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
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