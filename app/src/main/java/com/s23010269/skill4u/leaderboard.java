package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class leaderboard extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeaderboardAdapter adapter;
    private List<LeaderboardUser> userList = new ArrayList<>();
    private DatabaseReference databaseRef;
    private String currentUsername;
    private ImageView openmenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // Load username
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("USERNAME", null);
        if (getIntent().hasExtra("USERNAME")) {
            currentUsername = getIntent().getStringExtra("USERNAME");
        }

        if (currentUsername == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, signin.class));
            finish();
            return;
        }
        openmenu = findViewById(R.id.openmenu);
        recyclerView = findViewById(R.id.recyclerLeaderboard);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(userList);
        recyclerView.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");
        openmenu.setOnClickListener(v -> startActivity(new Intent(leaderboard.this, menu.class)));

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        databaseRef.orderByChild("totalPoints").limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            String name = userSnap.child("name").getValue(String.class);
                            Integer points = userSnap.child("totalPoints").getValue(Integer.class);
                            if (name != null && points != null) {
                                userList.add(new LeaderboardUser(name, points));
                            }
                        }

                        // Sort descending manually
                        Collections.sort(userList, (u1, u2) -> Integer.compare(u2.getTotalPoints(), u1.getTotalPoints()));
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(leaderboard.this, "Failed to load leaderboard.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
