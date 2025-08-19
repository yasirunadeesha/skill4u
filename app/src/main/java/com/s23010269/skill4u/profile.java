package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.biometric.BiometricManager;

public class profile extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private TextView getUsernameText, getNameText;
    private ImageView goToHomeBtn;
    private Button logoutBtn, deleteAccountBtn;
    private Switch fingerprintSwitch;

    private String username;
    private DatabaseReference usersRef;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // View bindings
        Button contactAdmin = findViewById(R.id.contactAdmin);
        getUsernameText = findViewById(R.id.get_username);
        getNameText = findViewById(R.id.get_name);
        goToHomeBtn = findViewById(R.id.gotohome);
        logoutBtn = findViewById(R.id.logout);
        deleteAccountBtn = findViewById(R.id.deleteAccount);
        fingerprintSwitch = findViewById(R.id.fingerprint_on);

        // SharedPreferences
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // Firebase Realtime DB reference
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Get username from Intent or SharedPreferences
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
            getUsernameText.setText("@GUEST");
            getNameText.setText("GUEST");
        }

        // Biometric/fingerprint
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            fingerprintSwitch.setEnabled(false);
            Toast.makeText(this, "Fingerprint not supported on this device", Toast.LENGTH_SHORT).show();
        } else if (username != null) {
            // Load saved fingerprint status
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

            fingerprintSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                usersRef.child(username).child("fingerprintEnabled").setValue(isChecked);
                prefs.edit().putBoolean("FINGERPRINT_" + username, isChecked).apply();
            });
        }

        // Go to home
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
                    finish();
                })
                .setNegativeButton("No", null)
                .show()
        );

        contactAdmin.setOnClickListener(v -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(android.net.Uri.parse("mailto:yasirunaddeesha8@gmail.com")); // Only email apps will handle this
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "SKILL4U Support");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi Admin,\n\nI need help with...");

            try {
                startActivity(Intent.createChooser(emailIntent, "Send Email to Admin..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });
        // Delete Account
        deleteAccountBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(profile.this)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (username != null) {
                            usersRef.child(username).removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(profile.this, "Account deleted", Toast.LENGTH_SHORT).show();
                                        prefs.edit().remove("USERNAME").apply();

                                        // Redirect to signin
                                        Intent intent = new Intent(profile.this, signin.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(profile.this, "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(profile.this, "No user logged in", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
