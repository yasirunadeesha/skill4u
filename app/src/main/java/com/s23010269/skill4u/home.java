package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class home extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private DatabaseHelper dbHelper;
    private TextView getName;
    private ImageView openMenu, profile;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getName = findViewById(R.id.get_name);
        openMenu = findViewById(R.id.openmenu);
        profile = findViewById(R.id.profile);

        dbHelper = new DatabaseHelper(this);

        // Try to get username from Intent
        String username = getIntent().getStringExtra("USERNAME");

        // If null, try from SharedPreferences (optional fallback)
        if (username == null) {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            username = prefs.getString("USERNAME", null);
        }

        currentUsername = username;

        if (username != null) {
            Log.d(TAG, "Logged-in username: " + username);
            String name = getNameFromDatabase(username);
            if (name != null) {
                getName.setText(name.toUpperCase());
            } else {
                getName.setText("USER");
            }
        } else {
            Log.w(TAG, "USERNAME not found, showing Guest");
            getName.setText("GUEST");
        }

        openMenu.setOnClickListener(v -> {
            Intent menuIntent = new Intent(home.this, menu.class);
            startActivity(menuIntent);
        });

        profile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(home.this, profile.class);
            profileIntent.putExtra("USERNAME", currentUsername);
            profileIntent.putExtra("NAME", getName.getText().toString());
            startActivity(profileIntent);
        });

        setupCalendar(); // Initialize calendar
    }

    private String getNameFromDatabase(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.USER_COL_NAME + " FROM " +
                DatabaseHelper.USER_TABLE + " WHERE " + DatabaseHelper.USER_COL_USERNAME + " = ?", new String[]{username});

        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    private void setupCalendar() {
        LinearLayout calendarLayout = findViewById(R.id.calendar_container);
        calendarLayout.removeAllViews();

        String[] daysOfWeek = {"M", "T", "W", "T", "F", "S", "S"};

        Calendar calendar = Calendar.getInstance();
        int todayDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int todayDate = calendar.get(Calendar.DAY_OF_MONTH);


        int offset = todayDayOfWeek - Calendar.MONDAY;
        if (offset < 0) offset += 7;
        calendar.add(Calendar.DATE, -offset);


        for (int i = 0; i < 7; i++) {
            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(48), dpToPx(64));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dayLayout.setLayoutParams(params);

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            boolean isToday = (dayOfWeek == Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
            int date = calendar.get(Calendar.DAY_OF_MONTH);

            dayLayout.setBackground(ContextCompat.getDrawable(this,
                    isToday ? R.drawable.day_active : R.drawable.day_inactive));

            TextView dayText = new TextView(this);
            dayText.setText(daysOfWeek[i]);
            dayText.setTextColor(0xFFFFFFFF);
            dayText.setTextSize(14f);
            dayText.setTypeface(null, Typeface.BOLD);
            dayText.setGravity(Gravity.CENTER);

            TextView dateText = new TextView(this);
            dateText.setText(String.valueOf(date));
            dateText.setTextColor(0xFFFFFFFF);
            dateText.setTextSize(14f);
            dateText.setGravity(Gravity.CENTER);

            dayLayout.addView(dayText);
            dayLayout.addView(dateText);
            calendarLayout.addView(dayLayout);

            calendar.add(Calendar.DATE, 1);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
