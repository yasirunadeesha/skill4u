package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChallengeActivity extends AppCompatActivity
        implements ChallengeAdapter.OnChallengeActionListener,
        ChallengeDetailDialogFragment.OnChallengeInteractionListener {

    private RecyclerView recyclerView;
    private ChallengeAdapter adapter;
    private List<Challenge> allChallenges; // All challenges list
    private List<Challenge> currentDisplayedChallenges;
    private RadioGroup challengeFilterRadioGroup; // RadioGroup for filtering

    private ImageView openMenu;
    private TextView titleText;
    private TextView yourChallengesText; // Text showing current challenge filter

    private String currentUsername;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge); // Set layout

        // Load username
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("USERNAME", null);
        if (getIntent().hasExtra("USERNAME")) {
            currentUsername = getIntent().getStringExtra("USERNAME");
        }

        // Redirect to sign-in if user not logged in
        if (currentUsername == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, signin.class));
            finish();
            return;
        }

        // Initialize Firebase reference for current user
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUsername);

        // UI Element Initialization
        openMenu = findViewById(R.id.openmenu);
        titleText = findViewById(R.id.textView7);
        yourChallengesText = findViewById(R.id.yourChallengesText);

        challengeFilterRadioGroup = findViewById(R.id.challengeFilterRadioGroup);
        recyclerView = findViewById(R.id.challengesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager

        // Sample challenge data
        allChallenges = new ArrayList<>();
        allChallenges.add(new Challenge(
                "c1", "Beginner Coding Challenge", "Beginner",
                "Solve 5 easy LeetCode problems in one week...",
                "https://leetcode.com/problemset/all/?difficulty=EASY",
                28, 50, false, false));
        allChallenges.add(new Challenge(
                "c2", "Intermediate Design Challenge", "Intermediate",
                "Create a homepage mockup in 48 hours using Figma...",
                "https://www.figma.com/", 15, 100, false, false));
        allChallenges.add(new Challenge(
                "c3", "Mindfulness Meditation", "Beginner",
                "Practice mindfulness meditation daily for a week...",
                "https://www.calm.com/", 40, 30, false, false));
        allChallenges.add(new Challenge(
                "c4", "Advanced Python Project", "Advanced",
                "Build a RESTful API with Flask/Django...",
                "https://flask.palletsprojects.com/", 10, 200, false, false));
        allChallenges.add(new Challenge(
                "c5", "Fitness: 30-Day Plank Challenge", "Beginner",
                "Hold a plank for increasing durations over 30 days...",
                "https://www.verywellfit.com/30-day-plank-challenge-4152564",
                60, 40, false, false));

        // show "Available" challenges
        filterChallenges("Available");

        adapter = new ChallengeAdapter(this, currentDisplayedChallenges, this); // Initialize adapter
        recyclerView.setAdapter(adapter); // Set adapter

        // Handle filter selection
        challengeFilterRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioAvailable) {
                filterChallenges("Available");
            } else if (checkedId == R.id.radioYour) {
                filterChallenges("Your");
            } else if (checkedId == R.id.radioCompleted) {
                filterChallenges("Completed");
            }
        });


        openMenu.setOnClickListener(v -> finish());
    }

    // Filter challenges based on type
    private void filterChallenges(String filterType) {
        currentDisplayedChallenges = new ArrayList<>();
        switch (filterType) {
            case "Available":
                currentDisplayedChallenges.addAll(allChallenges.stream()
                        .filter(ch -> !ch.isJoined())
                        .collect(Collectors.toList()));
                yourChallengesText.setText("Available Challenges");
                break;
            case "Your":
                currentDisplayedChallenges.addAll(allChallenges.stream()
                        .filter(ch -> ch.isJoined() && !ch.isCompleted())
                        .collect(Collectors.toList()));
                yourChallengesText.setText("Your Challenges");
                break;
            case "Completed":
                currentDisplayedChallenges.addAll(allChallenges.stream()
                        .filter(Challenge::isCompleted)
                        .collect(Collectors.toList()));
                yourChallengesText.setText("Completed Challenges");
                break;
        }
        if (adapter != null) {
            adapter.updateChallengeList(currentDisplayedChallenges); // Update adapter
        }
    }

    // User joins a challenge
    @Override
    public void onJoinChallenge(Challenge challenge) {
        challenge.setJoined(true);
        challenge.setUsersJoined(challenge.getUsersJoined() + 1);
        Toast.makeText(this, "You have joined: " + challenge.getTitle(), Toast.LENGTH_SHORT).show();
        filterChallenges(getSelectedFilterType()); // Refresh list
    }

    // User completes a challenge
    @Override
    public void onCompleteChallenge(Challenge challenge) {
        challenge.setCompleted(true);

        // Save completion date
        String completionDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        challenge.setCompletionDate(completionDate);

        // Save completed challenge in Firebase
        userRef.child("completedChallenges").child(challenge.getId()).setValue(challenge);

        // Update user's total points in Firebase
        userRef.child("totalPoints").get().addOnSuccessListener(snapshot -> {
            long currentPoints = 0;
            if (snapshot.exists()) {
                currentPoints = snapshot.getValue(Long.class);
            }
            long updatedPoints = currentPoints + challenge.getPoints();
            userRef.child("totalPoints").setValue(updatedPoints);
        });

        Toast.makeText(this, "You completed: " + challenge.getTitle() + "! Earned " + challenge.getPoints() + " points!", Toast.LENGTH_LONG).show();
        filterChallenges(getSelectedFilterType()); // Refresh list
    }

    // Open challenge detail dialog
    @Override
    public void onChallengeClick(Challenge challenge) {
        ChallengeDetailDialogFragment dialogFragment = ChallengeDetailDialogFragment.newInstance(challenge);
        dialogFragment.show(getSupportFragmentManager(), "ChallengeDetailDialog");
    }

    @Override
    public void onChallengeJoined(Challenge challenge) {
        onJoinChallenge(challenge); // Call join logic
    }

    // Get currently selected filter type
    private String getSelectedFilterType() {
        int checkedId = challengeFilterRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioAvailable) {
            return "Available";
        } else if (checkedId == R.id.radioYour) {
            return "Your";
        } else if (checkedId == R.id.radioCompleted) {
            return "Completed";
        }
        return "Available";
    }

}
