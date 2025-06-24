package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class signin extends AppCompatActivity {

    EditText username, password;
    Button signinBtn;
    TextView register, fingerprint;
    DatabaseHelper dbHelper;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signinBtn = findViewById(R.id.signin);
        register = findViewById(R.id.register);
        fingerprint = findViewById(R.id.fingerprint);
        dbHelper = new DatabaseHelper(this);
        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        signinBtn.setOnClickListener(v -> {
            String userInput = username.getText().toString().trim();
            String passInput = password.getText().toString().trim();

            if (userInput.isEmpty() || passInput.isEmpty()) {
                Toast.makeText(signin.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else if (checkUserCredentials(userInput, passInput)) {
                Toast.makeText(signin.this, "Login successful!", Toast.LENGTH_SHORT).show();


                prefs.edit().putString("USERNAME", userInput).apply();

                String name = getUserName(userInput);

                Intent homeIntent = new Intent(signin.this, home.class);
                homeIntent.putExtra("USERNAME", userInput);
                homeIntent.putExtra("NAME", name);
                startActivity(homeIntent);
                finish();
            } else {
                Toast.makeText(signin.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        register.setOnClickListener(v -> startActivity(new Intent(signin.this, signup.class)));

        fingerprint.setOnClickListener(v -> {
            String savedUsername = prefs.getString("USERNAME", null);
            if (savedUsername == null) {
                Toast.makeText(this, "Please log in at least once before using fingerprint", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean enabled = prefs.getBoolean("FINGERPRINT_" + savedUsername, false);
            if (enabled && isBiometricAvailable()) {
                showBiometricPrompt(savedUsername);
            } else {
                Toast.makeText(this, "Fingerprint login is not enabled or supported", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkUserCredentials(String username, String password) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM " + DatabaseHelper.USER_TABLE + " WHERE username = ? AND password = ?",
                new String[]{username, password}
        );
        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    private String getUserName(String username) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT name FROM " + DatabaseHelper.USER_TABLE + " WHERE username = ?",
                new String[]{username}
        );
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void showBiometricPrompt(String savedUsername) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(signin.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(signin.this, "Fingerprint login successful!", Toast.LENGTH_SHORT).show();

                String name = getUserName(savedUsername);

                Intent homeIntent = new Intent(signin.this, home.class);
                homeIntent.putExtra("USERNAME", savedUsername);  // consistent key
                homeIntent.putExtra("NAME", name);
                startActivity(homeIntent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(signin.this, "Fingerprint authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Login")
                .setSubtitle("Use your fingerprint to sign in")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
