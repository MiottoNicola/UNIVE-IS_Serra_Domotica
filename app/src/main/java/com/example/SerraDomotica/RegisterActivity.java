package com.example.SerraDomotica;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class RegisterActivity extends BaseActivity {

    private EditText nomeCognomeEditText, emailEditText, password1EditText, password2EditText;
    private FirebaseAuth mAuth;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference rootRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        nomeCognomeEditText = findViewById(R.id.nome_cognome);
        emailEditText = findViewById(R.id.email);
        password1EditText = findViewById(R.id.password1);
        password2EditText = findViewById(R.id.password2);
        Button registerButton = findViewById(R.id.register_button);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(null);

        registerButton.setOnClickListener(v -> {
            String nomeCognome = nomeCognomeEditText.getText().toString();
            String email = emailEditText.getText().toString();
            String password = password1EditText.getText().toString();
            String password2 = password2EditText.getText().toString();

            if (nomeCognome.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
                Toast.makeText(RegisterActivity.this, getString(R.string.emptyField_toastText), Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(password2)) {
                Toast.makeText(RegisterActivity.this, getString(R.string.passwordNotMatch_toastText), Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Registration success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user == null) {
                                Toast.makeText(RegisterActivity.this, getString(R.string.registerFailed_toastText), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DatabaseReference u = rootRef.child("users").child(user.getUid());
                            u.child("nome_cognome").setValue(nomeCognome);
                            u.child("email").setValue(email);

                            Toast.makeText(RegisterActivity.this, getString(R.string.registerSuccess_toastText), Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            Toast.makeText(RegisterActivity.this, getString(R.string.registerFailed_toastText), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        TextView termsConditions = findViewById(R.id.terms_conditions);
        termsConditions.setOnClickListener(v -> showTermsConditionsDialog());

    }

    private void showTermsConditionsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_terms_conditions);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        TextView closeButton = dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}