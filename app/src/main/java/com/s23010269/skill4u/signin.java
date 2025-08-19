package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;  // Add this import
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

public class signin extends AppCompatActivity {

    EditText usernameField, passwordField;
    Button signinButton;
    TextView registerText, fingerprintText;
    DatabaseReference databaseRef;
    SharedPreferences prefs;

    Executor executor;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        signinButton = findViewById(R.id.signin);
        registerText = findViewById(R.id.register);
        fingerprintText = findViewById(R.id.fingerprint);

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        signinButton.setOnClickListener(view -> loginWithPassword());
        fingerprintText.setOnClickListener(view -> loginWithFingerprint());

        registerText.setOnClickListener(view ->
                startActivity(new Intent(signin.this, signup.class))
        );

        // Setup biometric prompt
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(signin.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                String username = usernameField.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(signin.this, "Enter username first", Toast.LENGTH_SHORT).show();
                    return;
                }

                loginUser(username); // Directly log in after successful fingerprint
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                Toast.makeText(signin.this, "Fingerprint error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                Toast.makeText(signin.this, "Fingerprint not recognized", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Authenticate to log in")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void loginWithPassword() {
        final String username = usernameField.getText().toString().trim();
        final String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(signin.this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedPassword = snapshot.child("password").getValue(String.class);
                    if (storedPassword != null && storedPassword.equals(password)) {
                        saveLoginAndGoHome(username);
                    } else {
                        Toast.makeText(signin.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(signin.this, "Username not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(signin.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginWithFingerprint() {
        final String username = usernameField.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Enter your username first", Toast.LENGTH_SHORT).show();
            return;
        }

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                != BiometricManager.BIOMETRIC_SUCCESS) {
            Toast.makeText(this, "Fingerprint login not available on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.child(username).child("fingerprintEnabled")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isEnabled = snapshot.getValue(Boolean.class);
                        if (isEnabled != null && isEnabled) {
                            biometricPrompt.authenticate(promptInfo);
                        } else {
                            Toast.makeText(signin.this, "Fingerprint login not enabled for this user", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(signin.this, "Error checking fingerprint status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loginUser(String username) {
        databaseRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    saveLoginAndGoHome(username);
                } else {
                    Toast.makeText(signin.this, "No user found for fingerprint login", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(signin.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLoginAndGoHome(String username) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("USERNAME", username);
        editor.putString("USERID", username);
        editor.apply();

        // Save login time and increment daily login count
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        DatabaseReference userLoginsRef = databaseRef.child(username).child("loginHistory").child(currentDate);
        DatabaseReference loginCountRef = databaseRef.child(username).child("dailyLoginCount").child(currentDate);

        // Push the login time
        userLoginsRef.push().setValue(currentTime);

        // Increment login count
        loginCountRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer count = currentData.getValue(Integer.class);
                if (count == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue(count + 1);
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                // Optional: handle completion
            }
        });

        Toast.makeText(signin.this, "Login successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(signin.this, home.class);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish();
    }
}
