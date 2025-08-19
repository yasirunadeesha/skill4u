package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class analytics extends AppCompatActivity {

    private static final String TAG = "AnalyticsActivity";

    private ImageView openmenu;
    private TextView currentStreakTextView, bestStreakTextView;
    private TextView skillsWeekTextView, skillsMonthTextView, skillsYearTextView;
    private CalendarView calendarView;
    private TextView totalPointsValue, dailyPointsTextView, dailyActiveTimeTextView;
    private LinearLayout achievementsContainer;

    private DatabaseReference databaseReference;
    private String currentUsername;
    private DataSnapshot currentUserSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Get reference to "Users" node in Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Get the logged-in username from intent or shared preferences
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null) {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            currentUsername = prefs.getString("USERNAME", null);
        }


        openmenu              = findViewById(R.id.openmenu);
        currentStreakTextView = findViewById(R.id.currentStreakTextView);
        bestStreakTextView    = findViewById(R.id.bestStreakTextView);
        skillsWeekTextView    = findViewById(R.id.skillsWeekTextView);
        skillsMonthTextView   = findViewById(R.id.skillsMonthTextView);
        skillsYearTextView    = findViewById(R.id.skillsYearTextView);
        calendarView          = findViewById(R.id.calendarView);
        totalPointsValue      = findViewById(R.id.totalPointsValue);
        dailyPointsTextView   = findViewById(R.id.dailyPointsTextView);
        dailyActiveTimeTextView = findViewById(R.id.dailyActiveTimeTextView);
        achievementsContainer = findViewById(R.id.achievements_container);

        // Load user data from Firebase if username is available
        if (currentUsername != null) {
            loadUserData();
        }

        // Open menu
        openmenu.setOnClickListener(v -> startActivity(new Intent(analytics.this, menu.class)));
    }

    // Finds the user by username and then fetches analytics data
    private void loadUserData() {
        databaseReference.orderByChild("username").equalTo(currentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot userSnap : snapshot.getChildren()) {
                            currentUserSnapshot = userSnap;

                            // Fetch total points and achievements
                            fetchTotalPoints(userSnap);
                            fetchAchievements(userSnap);

                            // Set calendar listener for daily data
                            setupCalendarViewListener(userSnap);

                            // Show today's data by default
                            long todayMillis = calendarView.getDate();
                            fetchDailyData(userSnap, todayMillis);
                            break;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error finding user", error.toException());
                    }
                });
    }

    // Fetch and display total points
    private void fetchTotalPoints(DataSnapshot userSnap) {
        Integer totalPoints = userSnap.child("totalPoints").getValue(Integer.class);
        if (totalPoints != null) {
            totalPointsValue.setText(String.valueOf(totalPoints));
        }
    }

    // Fetch and display all achievements
    private void fetchAchievements(DataSnapshot userSnap) {
        achievementsContainer.removeAllViews();
        DataSnapshot challengesSnap = userSnap.child("completedChallenges");
        for (DataSnapshot chSnap : challengesSnap.getChildren()) {
            String title = chSnap.child("title").getValue(String.class);
            if (title != null) {
                TextView tv = new TextView(this);
                tv.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                tv.setText(title);
                tv.setTextColor(getResources().getColor(android.R.color.white));
                tv.setTextSize(16);
                tv.setPadding(0, 8, 0, 8);
                achievementsContainer.addView(tv);
            }
        }
    }

    // Sets up listener for calendar date changes
    private void setupCalendarViewListener(DataSnapshot userSnap) {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            fetchDailyData(userSnap, cal.getTimeInMillis());
        });
    }

    // Fetch and display daily points and active time
    private void fetchDailyData(DataSnapshot userSnap, long dateMillis) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateKey = dateFormat.format(dateMillis);

        // Calculate daily points
        int dailyPoints = 0;
        for (DataSnapshot chSnap : userSnap.child("completedChallenges").getChildren()) {
            String completionDate = chSnap.child("completionDate").getValue(String.class);
            if (completionDate != null && completionDate.equals(dateKey)) {
                Integer pts = chSnap.child("points").getValue(Integer.class);
                if (pts != null) dailyPoints += pts;
            }
        }
        dailyPointsTextView.setText("Points Earned: " + dailyPoints);

        // Calculate daily active time
        long activeTimeMinutes = 0;
        DataSnapshot loginDaySnap = userSnap.child("loginHistory").child(dateKey);
        if (loginDaySnap.exists()) {
            String firstLogin = null;
            String lastLogin  = null;
            for (DataSnapshot timeSnap : loginDaySnap.getChildren()) {
                if (firstLogin == null) firstLogin = timeSnap.getValue(String.class);
                lastLogin = timeSnap.getValue(String.class);
            }
            if (firstLogin != null && lastLogin != null) {
                try {
                    SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    long start = tf.parse(firstLogin).getTime();
                    long end   = tf.parse(lastLogin).getTime();
                    activeTimeMinutes = (end - start) / (1000 * 60); // convert ms to minutes
                } catch (ParseException e) {
                    Log.e(TAG, "time parse error", e);
                }
            }
        }
        dailyActiveTimeTextView.setText("Active Time: " + activeTimeMinutes + " min");
    }
}
