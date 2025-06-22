package com.s23010269.skill4u;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class signup extends AppCompatActivity {

    private EditText username, name, email, password, confirmpass;
    private Button signupButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        username = findViewById(R.id.username);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmpass = findViewById(R.id.confirmpass);
        signupButton = findViewById(R.id.signup);

        dbHelper = new DatabaseHelper(this);

        signupButton.setOnClickListener(view -> {
            String user = username.getText().toString().trim();
            String fullName = name.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirmPass = confirmpass.getText().toString().trim();

            if (user.isEmpty() || fullName.isEmpty() || userEmail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(signup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(signup.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.isUsernameExists(user)) {
                Toast.makeText(signup.this, "Username already taken. Try another one.", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean insertSuccess = dbHelper.insertUser(user, fullName, userEmail, pass);
            if (insertSuccess) {
                Toast.makeText(signup.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(signup.this, signin.class));
                finish();
            } else {
                Toast.makeText(signup.this, "Registration Failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
