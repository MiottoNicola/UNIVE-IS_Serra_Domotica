package com.example.SerraDomotica;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SerraDomotica.utils.ConnectionMonitor;
import com.example.SerraDomotica.utils.OfflineDialog;

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
}
