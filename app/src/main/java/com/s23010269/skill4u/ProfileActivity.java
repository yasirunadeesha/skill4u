package com.s23010269.skill4u;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileText;
    private String uid;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // We'll define this next

        profileText = findViewById(R.id.profileText);

        uid = getIntent().getStringExtra("uid");
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                profileText.setText("User: " + name);
            }

            public void onCancelled(DatabaseError error) {
                profileText.setText("Failed to load profile");
            }
        });
    }
}
