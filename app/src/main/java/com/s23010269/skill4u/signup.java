package com.s23010269.skill4u;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class signup extends AppCompatActivity {

    EditText username, name, email, password, confirmpass;
    Button signup;
    DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Test Firebase connection
        FirebaseDatabase.getInstance().getReference("DebugTest").setValue("Connected");

        // Initialize Views
        username = findViewById(R.id.username);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmpass = findViewById(R.id.confirmpass);
        signup = findViewById(R.id.signup);

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String user = username.getText().toString().trim();
                final String fullName = name.getText().toString().trim();
                final String userEmail = email.getText().toString().trim();
                final String userPass = password.getText().toString().trim();
                final String confirmPass = confirmpass.getText().toString().trim();

                // Check if all fields are filled
                if (TextUtils.isEmpty(user) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(userEmail) ||
                        TextUtils.isEmpty(userPass) || TextUtils.isEmpty(confirmPass)) {
                    Toast.makeText(signup.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if passwords match
                if (!userPass.equals(confirmPass)) {
                    Toast.makeText(signup.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if username already exists
                databaseRef.child(user).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(signup.this, "Username already exists, choose another", Toast.LENGTH_SHORT).show();
                        } else {
                            // Save new user
                            User newUser = new User(user, fullName, userEmail, userPass);
                            databaseRef.child(user).setValue(newUser).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(signup.this, signin.class);
                                    startActivity(intent);
                                    finish(); // Optional
                                } else {
                                    Toast.makeText(signup.this, "Signup failed. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(signup.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
