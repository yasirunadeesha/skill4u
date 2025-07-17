package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class profile extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    TextView getUsernameText, getNameText;
    ImageView goToHomeBtn, logoutBtn;
    Switch fingerprintSwitch;

    private String username;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // View bindings
        getUsernameText = findViewById(R.id.get_username);
        getNameText = findViewById(R.id.get_name);
        goToHomeBtn = findViewById(R.id.gotohome);
        logoutBtn = findViewById(R.id.logout);
        fingerprintSwitch = findViewById(R.id.fingerprint_on);

        // Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Load username
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            username = prefs.getString("USERNAME", null);
        }

        if (username != null) {
            getUsernameText.setText("@" + username);

            // Load name from Firebase
            usersRef.child(username).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.getValue(String.class);
                            getNameText.setText(name != null ? name : "USER");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Failed to fetch name", error.toException());
                            getNameText.setText("USER");
                        }
                    });
        } else {
            getNameText.setText("GUEST");
        }

        // Check for biometric support
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            fingerprintSwitch.setEnabled(false);
            Toast.makeText(this, "Fingerprint not supported on this device", Toast.LENGTH_SHORT).show();
        } else {
            // Load fingerprint status from Firebase
            usersRef.child(username).child("fingerprintEnabled")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean enabled = snapshot.getValue(Boolean.class);
                            fingerprintSwitch.setChecked(enabled != null && enabled);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Error reading fingerprint status", error.toException());
                        }
                    });

            // Save to Firebase and SharedPreferences when switched
            fingerprintSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
                usersRef.child(username).child("fingerprintEnabled").setValue(isChecked);
                prefs.edit().putBoolean("FINGERPRINT_" + username, isChecked).apply();
            });
        }

        // Go to Home
        goToHomeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(profile.this, home.class);
            intent.putExtra("USERNAME", username);
            startActivity(intent);
            finish();
        });

        // Logout
        logoutBtn.setOnClickListener(v -> new AlertDialog.Builder(profile.this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dlg, which) -> {
                    prefs.edit().remove("USERNAME").apply();
                    startActivity(new Intent(profile.this, signin.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .setNegativeButton("No", null)
                .show()
        );
    }
}
