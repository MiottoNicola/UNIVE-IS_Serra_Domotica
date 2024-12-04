package com.example.SerraDomotica;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SerraDomotica.utils.ConnectionMonitor;
import com.example.SerraDomotica.utils.OfflineDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public abstract class BaseActivity extends AppCompatActivity {

    private ConnectionMonitor connectionMonitor;
    private OfflineDialog offlineDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        offlineDialog = new OfflineDialog(this);

        connectionMonitor = new ConnectionMonitor(this, isConnected -> {
            if (isConnected) {
                Log.d("InternetCheck", "Connection established");
                if (offlineDialog.isShowing()) {
                    offlineDialog.dismiss();
                }
            } else {
                Log.d("InternetCheck", "Connection lost");
                if (!offlineDialog.isShowing()) {
                    offlineDialog.show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectionMonitor.startMonitoring(); // Avvia il monitoraggio
    }

    @Override
    protected void onPause() {
        super.onPause();
        connectionMonitor.stopMonitoring(); // Interrompi il monitoraggio
    }

    protected void addGreenhouse(String greenhouseId, Context context) {
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("devices").child(greenhouseId);
        deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean isConnected = dataSnapshot.child("config").child("isConnected").getValue(Boolean.class);
                    if (isConnected != null && !isConnected) {
                        // Update isConnected to true
                        deviceRef.child("config").child("isConnected").setValue(true).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                DatabaseReference userDevicesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("devices");
                                userDevicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot userDevicesSnapshot) {
                                        if (!userDevicesSnapshot.exists()) {
                                            userDevicesRef.setValue(true).addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    addGreenhouseToUserDevices(userDevicesRef, greenhouseId, context);
                                                } else {
                                                    Toast.makeText(context, context.getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            addGreenhouseToUserDevices(userDevicesRef, greenhouseId, context);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Toast.makeText(context, context.getString(R.string.failedLoadData_toastText), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(context, context.getString(R.string.failedSaveData_toastText), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(context, context.getString(R.string.greenhouseAlredyConnected_toastText), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, context.getString(R.string.noGreenhousesFound_text), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, context.getString(R.string.failedSaveData_toastText), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addGreenhouseToUserDevices(DatabaseReference userDevicesRef, String greenhouseId, Context context) {
        userDevicesRef.child(greenhouseId).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, context.getString(R.string.succConnectGreenhouse_toastText), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, context.getString(R.string.failedConnectGreenhouse_toastText), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
