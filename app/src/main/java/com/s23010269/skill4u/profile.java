package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class profile extends AppCompatActivity {

    TextView getUsernameText, getNameText, fingerText;
    ImageView goToHomeBtn, logoutBtn;
    Switch fingerprintSwitch;

    private String username, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // View references
        getUsernameText = findViewById(R.id.get_username);
        getNameText = findViewById(R.id.get_name);
        fingerText = findViewById(R.id.fingertext);
        goToHomeBtn = findViewById(R.id.gotohome);
        logoutBtn = findViewById(R.id.logout);
        fingerprintSwitch = findViewById(R.id.locationon);

        // Retrieve data from Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("USERNAME");
        name = intent.getStringExtra("NAME");

        // Set user info
        if (username != null) {
            getUsernameText.setText("@" + username);
        }
        if (name != null) {
            getNameText.setText(name);
        }

        // Load fingerprint toggle state from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isFingerprintEnabled = prefs.getBoolean("FINGERPRINT_" + username, false);
        fingerprintSwitch.setChecked(isFingerprintEnabled);

        // Save toggle state
        fingerprintSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("FINGERPRINT_" + username, isChecked).apply();
        });

        // Navigate to Home
        goToHomeBtn.setOnClickListener(v -> {
            Intent homeIntent = new Intent(profile.this, home.class);
            homeIntent.putExtra("USERNAME", username);
            homeIntent.putExtra("NAME", name);
            startActivity(homeIntent);
            finish();
        });

        // Logout
        logoutBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(profile.this)
                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear session info if needed
                        Intent logoutIntent = new Intent(profile.this, signin.class);
                        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(logoutIntent);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
}
