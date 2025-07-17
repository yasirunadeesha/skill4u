package com.s23010269.skill4u;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup; // Import ViewGroup

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class ChallengeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChallengeAdapter adapter;
    private List<Challenge> challengeList;

    private ImageView openMenu;
    private TextView titleText;
    private TextView yourChallengesText;
    // private TextView allChallengesText; // This TextView is not present in activity_challenge.xml, consider removing if not used.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        openMenu = findViewById(R.id.openmenu);
        titleText = findViewById(R.id.textView7);
        yourChallengesText = findViewById(R.id.yourChallengesText);
        // allChallengesText = findViewById(R.id.allChallengesText); // This ID does not exist in activity_challenge.xml

        recyclerView = findViewById(R.id.challengesRecyclerView); // Correct way to find RecyclerView by ID from XML
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Sample challenge data
        challengeList = new ArrayList<>();
        // Ensure the Challenge constructor matches the Challenge.java class
        challengeList.add(new Challenge(
                "c1", // Added ID
                "Beginner Coding Challenge",
                "Beginner",
                "Solve 5 easy LeetCode problems in one week.",
                28));
        challengeList.add(new Challenge(
                "c2", // Added ID
                "Intermediate Design Challenge",
                "Intermediate",
                "Create a website homepage mockup in 48 hours using Figma.",
                15));
        challengeList.add(new Challenge(
                "c3", // Added ID
                "Mindfulness Challenge",
                "Beginner",
                "Practice mindfulness meditation for 10 minutes daily for a week.",
                40));

        adapter = new ChallengeAdapter(this, challengeList);
        recyclerView.setAdapter(adapter);

        // You might want to handle clicks for openMenu, yourChallengesText, etc. here
        // For example:
        // openMenu.setOnClickListener(v -> { /* Open navigation drawer or perform action */ });
    }
}