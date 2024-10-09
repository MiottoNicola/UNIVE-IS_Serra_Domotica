package com.example.SerraDomotica;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity {

    private TextView textNomeCognome;
    private TextView textEmail;
    private Button buttonResetPassword;
    private ImageView backArrow;
    private ImageView logoutIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textNomeCognome = findViewById(R.id.textNomeCognome);
        textEmail = findViewById(R.id.textEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        logoutIcon = findViewById(R.id.logoutIcon);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(null);

        // Dummy data for demonstration
        textNomeCognome.setText("Nicola Miotto");
        textEmail.setText("nicola.miotto@example.com");

        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle reset password logic here
                Toast.makeText(ProfileActivity.this, "Reset password clicked", Toast.LENGTH_SHORT).show();
            }
        });

        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();
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