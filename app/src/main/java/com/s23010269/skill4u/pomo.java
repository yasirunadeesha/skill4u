package com.s23010269.skill4u;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class pomo extends AppCompatActivity {

    private TextView timerText, recordsText;
    private Button startButton;
    private MaterialButton btnStopwatch, btnCountdown;
    private MaterialButtonToggleGroup toggleGroup;
    private ConstraintLayout rootLayout;
    private ImageView openMenu;

    private boolean isCountdown = false;
    private boolean isRunning = false;

    private long timeLeft = 25 * 60 * 1000; // 25 minutes in ms
    private CountDownTimer countDownTimer;
    private Chronometer chronometer;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomo);

        timerText = findViewById(R.id.timer_text);
        recordsText = findViewById(R.id.records); // corrected ID from your xml
        startButton = findViewById(R.id.start_button);
        btnStopwatch = findViewById(R.id.btn_stopwatch);
        btnCountdown = findViewById(R.id.btn_countdown);
        toggleGroup = findViewById(R.id.toggle_group);
        openMenu = findViewById(R.id.openmenu);
        rootLayout = findViewById(R.id.pomoLayout); // add android:id="@+id/pomoLayout" to root layout in XML!

        dbHelper = new DatabaseHelper(this);
        chronometer = new Chronometer(this);

        // Set default selection to Stopwatch and UI setup
        toggleGroup.check(R.id.btn_stopwatch);
        isCountdown = false;
        timerText.setText("00:00");
        updateBackgroundColors();

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_stopwatch) {
                isCountdown = false;
                timerText.setText("00:00");
            } else if (checkedId == R.id.btn_countdown) {
                isCountdown = true;
                timeLeft = 25 * 60 * 1000; // reset countdown
                timerText.setText("25:00");
            }
            updateBackgroundColors();
            stopTimers();
            isRunning = false;
            startButton.setText("START");
        });

        startButton.setOnClickListener(view -> {
            if (!isRunning) {
                if (isCountdown) {
                    startCountdown();
                } else {
                    startStopwatch();
                }
                isRunning = true;
                startButton.setText("STOP");
            } else {
                stopTimers();
                isRunning = false;
                startButton.setText("START");
                showAllRecords();
            }
        });

        openMenu.setOnClickListener(view -> {
            Intent intent = new Intent(pomo.this, menu.class);
            startActivity(intent);
        });

        // Load records on start
        showAllRecords();
    }

    private void updateBackgroundColors() {
        if (isCountdown) {
            btnCountdown.setBackgroundTintList(getColorStateList(R.color.blue));
            btnStopwatch.setBackgroundTintList(getColorStateList(R.color.dark_grey));
        } else {
            btnStopwatch.setBackgroundTintList(getColorStateList(R.color.blue));
            btnCountdown.setBackgroundTintList(getColorStateList(R.color.dark_grey));
        }
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateCountdownText();
            }
            public void onFinish() {
                timeLeft = 0;
                updateCountdownText();
                saveRecord("Pomodoro", "25:00");
                Toast.makeText(pomo.this, "Countdown finished!", Toast.LENGTH_SHORT).show();
                isRunning = false;
                startButton.setText("START");
                showAllRecords();
            }
        }.start();
    }

    private void updateCountdownText() {
        int minutes = (int) (timeLeft / 1000) / 60;
        int seconds = (int) (timeLeft / 1000) % 60;
        timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
    }

    private void startStopwatch() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setOnChronometerTickListener(c -> {
            long elapsedMillis = SystemClock.elapsedRealtime() - c.getBase();
            int minutes = (int) (elapsedMillis / 1000) / 60;
            int seconds = (int) (elapsedMillis / 1000) % 60;
            timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
        });
        chronometer.start();
    }

    private void stopTimers() {
        if (chronometer != null) {
            chronometer.stop();
            if (!isCountdown) {
                long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
                int minutes = (int) (elapsedMillis / 1000) / 60;
                int seconds = (int) (elapsedMillis / 1000) % 60;
                String duration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                saveRecord("Stopwatch", duration);
            }
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void saveRecord(String type, String duration) {
        boolean success = dbHelper.insertTimeRecord(type, duration);
        if (!success) {
            Toast.makeText(this, "Failed to save record.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllRecords() {
        List<String> records = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseHelper.TIME_RECORDS_COL_TYPE + ", " +
                DatabaseHelper.TIME_RECORDS_COL_DURATION + " FROM " + DatabaseHelper.TIME_RECORDS_TABLE +
                " ORDER BY " + DatabaseHelper.TIME_RECORDS_COL_ID + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(0);
                String duration = cursor.getString(1);
                records.add(type + " - " + duration);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        if (records.isEmpty()) {
            recordsText.setText("No Recent Records");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String r : records) {
                sb.append(r).append("\n");
            }
            recordsText.setText(sb.toString().trim());
        }
    }
}
