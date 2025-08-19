package com.s23010269.skill4u;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class achievements extends AppCompatActivity {

    private DatabaseReference databaseRef; // Firebase reference
    private String userId = "1"; // Placeholder user ID
    private ImageView openmenu; // Menu button

    private int totalPoints = 0;
    private int loginStreak = 0;
    private int challengesCompleted = 0;

    private ListView listView; // show achievements
    private RadioGroup toggleGroup; // switch between completed/uncompleted

    // All achievements stored here
    private final Map<String, Achievement> achievementList = new LinkedHashMap<>();
    private final List<String> earnedAchievementKeys = new ArrayList<>();
    private final List<Achievement> completedAchievements = new ArrayList<>();
    private final List<Achievement> uncompletedAchievements = new ArrayList<>();

    // Achievement model
    class Achievement {
        String key;
        String title;
        boolean isCompleted;
        int pointReward;
        String dateCompleted;

        Achievement(String key, String title, int pointReward) {
            this.key = key;
            this.title = title;
            this.pointReward = pointReward;
            this.isCompleted = false;
        }

        // Convert achievement data to a map
        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("pointReward", pointReward);
            map.put("dateCompleted", dateCompleted);
            return map;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        // Initialize views
        openmenu = findViewById(R.id.openmenu);
        listView = findViewById(R.id.achievementListView);
        toggleGroup = findViewById(R.id.segmentGroup);

        databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        setupAchievements(); // Add all achievements
        loadUserData(); // Load user points, streaks, completed achievements

        // Toggle for completed/uncompleted achievements
        toggleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_complete) {
                showAchievements(true);
            } else if (checkedId == R.id.rb_incomplete) {
                showAchievements(false);
            }
        });

        // Open menu
        openmenu.setOnClickListener(v -> startActivity(new Intent(achievements.this, menu.class)));
    }

    // all achievements
    private void setupAchievements() {
        addAchievement("First Step", "First Step: Log in to the app for the first time.", 5);
        addAchievement("Daily Devotee", "Daily Devotee: Log in for 1 day.", 10);
        addAchievement("Week Warrior", "Week Warrior: 7-day login streak.", 20);
        addAchievement("Month Master", "Month Master: 30-day login streak.", 50);
        addAchievement("Quarter Conqueror", "Quarter Conqueror: 90-day login streak.", 100);
        addAchievement("Half-Year Hero", "Half-Year Hero: 180-day login streak.", 200);
        addAchievement("Yearly Yoda", "Yearly Yoda: 360-day login streak.", 500);
        addAchievement("Double Duty", "Double Duty: 2-year login streak.", 1000);
        addAchievement("Point Prodigy", "Point Prodigy: Earn 10 points.", 5);
        addAchievement("Century Star", "Century Star: Earn 100 points.", 10);
        addAchievement("Millennial Maven", "Millennial Maven: Earn 1,000 points.", 20);
        addAchievement("Point Powerhouse", "Point Powerhouse: Earn 5,000 points.", 50);
        addAchievement("Elite Earner", "Elite Earner: Earn 10,000 points.", 100);
        addAchievement("Lesson Learner", "Lesson Learner: Complete 1 challenge.", 5);
        addAchievement("Knowledge Knight", "Knowledge Knight: Complete 10 challenges.", 15);
        addAchievement("Wisdom Wizard", "Wisdom Wizard: Complete 50 challenges.", 30);
        addAchievement("Master Mind", "Master Mind: Complete 100 challenges.", 50);
    }

    // Helper to add achievement to the map
    private void addAchievement(String key, String title, int pointReward) {
        achievementList.put(key, new Achievement(key, title, pointReward));
    }

    // Load user's saved data from Firebase
    private void loadUserData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Load user stats
                    Integer tp = snapshot.child("totalPoints").getValue(Integer.class);
                    Integer ls = snapshot.child("loginStreak").getValue(Integer.class);
                    Integer cc = snapshot.child("challengesCompleted").getValue(Integer.class);

                    totalPoints = tp != null ? tp : 0;
                    loginStreak = ls != null ? ls : 0;
                    challengesCompleted = cc != null ? cc : 0;

                    // Load earned achievement keys
                    if (snapshot.hasChild("achievementsEarned")) {
                        for (DataSnapshot ds : snapshot.child("achievementsEarned").getChildren()) {
                            earnedAchievementKeys.add(ds.getKey());
                        }
                    }

                    checkAchievements(); // Update achievements based on data
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(achievements.this, "Failed to load data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check which achievements are completed
    private void checkAchievements() {
        completedAchievements.clear();
        uncompletedAchievements.clear();

        Map<String, Object> earnedMap = new HashMap<>();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (Map.Entry<String, Achievement> entry : achievementList.entrySet()) {
            String key = entry.getKey();
            Achievement ach = entry.getValue();

            if (earnedAchievementKeys.contains(key)) {
                ach.isCompleted = true;
                completedAchievements.add(ach);
                continue;
            }

            // Check if user meets the criteria
            if (shouldBeCompleted(key)) {
                ach.isCompleted = true;
                ach.dateCompleted = date;
                totalPoints += ach.pointReward;
                earnedAchievementKeys.add(key);
                completedAchievements.add(ach);

                earnedMap.put(key, ach.toMap());
            } else {
                uncompletedAchievements.add(ach);
            }
        }

        // Save updated points and achievements to Firebase
        databaseRef.child("totalPoints").setValue(totalPoints);
        databaseRef.child("achievementsEarned").updateChildren(earnedMap);

        showAchievements(false); // Default: show uncompleted
    }

    // Determine if an achievement should be completed
    private boolean shouldBeCompleted(String key) {
        switch (key) {
            case "First Step":
            case "Daily Devotee": return true; // Always completed
            case "Week Warrior": return loginStreak >= 7;
            case "Month Master": return loginStreak >= 30;
            case "Quarter Conqueror": return loginStreak >= 90;
            case "Half-Year Hero": return loginStreak >= 180;
            case "Yearly Yoda": return loginStreak >= 360;
            case "Double Duty": return loginStreak >= 720;
            case "Point Prodigy": return totalPoints >= 10;
            case "Century Star": return totalPoints >= 100;
            case "Millennial Maven": return totalPoints >= 1000;
            case "Point Powerhouse": return totalPoints >= 5000;
            case "Elite Earner": return totalPoints >= 10000;
            case "Lesson Learner": return challengesCompleted >= 1;
            case "Knowledge Knight": return challengesCompleted >= 10;
            case "Wisdom Wizard": return challengesCompleted >= 50;
            case "Master Mind": return challengesCompleted >= 100;
            default: return false;
        }
    }

    // Display completed or uncompleted achievements
    private void showAchievements(boolean showCompleted) {
        List<Achievement> listToShow = showCompleted ? completedAchievements : uncompletedAchievements;
        AchievementAdapter adapter = new AchievementAdapter(this, listToShow);
        listView.setAdapter(adapter);
    }
}
