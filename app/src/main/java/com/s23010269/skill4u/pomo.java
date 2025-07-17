package com.s23010269.skill4u;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

import java.util.*;

public class pomo extends AppCompatActivity {

    private TextView timerText, recordsText;
    private Button startPauseBtn;
    private ImageView openmenu;
    private FrameLayout timerCircle;
    private LinearLayout controlButtonsLayout;
    private LinearLayout pauseButtonsLayout;

    private CountDownTimer countDownTimer;
    private long selectedMillis = 25 * 60 * 1000;
    private boolean isRunning = false;
    private boolean isPaused = false;
    private long timeLeft = selectedMillis;

    private String username;
    private DatabaseReference focusRef;
    private final List<String> focusHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomo);

        timerText = findViewById(R.id.timer_text);
        recordsText = findViewById(R.id.records);
        startPauseBtn = findViewById(R.id.start_button);
        timerCircle = findViewById(R.id.timer_circle);
        openmenu = findViewById(R.id.openmenu);
        controlButtonsLayout = findViewById(R.id.control_buttons);

        // Get username from intent or prefs
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            username = prefs.getString("USERNAME", "GUEST");
        }

        focusRef = FirebaseDatabase.getInstance().getReference("FocusRecords").child(username);
        loadFocusRecords();

        // Menu
        openmenu.setOnClickListener(v -> {
            startActivity(new Intent(pomo.this, menu.class));
        });

        // Time picker
        timerText.setOnClickListener(v -> openTimePicker());

        // Start/Pause
        startPauseBtn.setOnClickListener(v -> {
            if (!isRunning && !isPaused) {
                startTimer(timeLeft);
            } else if (isRunning) {
                pauseTimer();
            }
        });
    }

    private void openTimePicker() {
        int mins = (int) (timeLeft / 60000);
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            timeLeft = minute * 60 * 1000L;
            selectedMillis = timeLeft;
            updateTimerDisplay(timeLeft);
        }, 0, mins, true).show();
    }

    private void startTimer(long millis) {
        countDownTimer = new CountDownTimer(millis, 1000) {
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                updateTimerDisplay(timeLeft);
            }

            public void onFinish() {
                isRunning = false;
                updateTimerDisplay(0);
                recordFocusTime((int) (selectedMillis / 60000));
                resetButtons();
            }
        }.start();

        isRunning = true;
        isPaused = false;
        startPauseBtn.setText("PAUSE");
    }

    private void pauseTimer() {
        countDownTimer.cancel();
        isRunning = false;
        isPaused = true;
        showPauseOptions();
    }

    private void showPauseOptions() {
        // Hide Start button
        startPauseBtn.setVisibility(View.GONE);

        // Create pause button layout if not already created
        if (pauseButtonsLayout == null) {
            pauseButtonsLayout = new LinearLayout(this);
            pauseButtonsLayout.setOrientation(LinearLayout.HORIZONTAL);
            pauseButtonsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            pauseButtonsLayout.setGravity(View.TEXT_ALIGNMENT_CENTER);
            pauseButtonsLayout.setPadding(10, 0, 10, 0);

            Button continueBtn = new Button(this);
            continueBtn.setText("CONTINUE");
            continueBtn.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
            continueBtn.setTextColor(getResources().getColor(android.R.color.white));
            continueBtn.setOnClickListener(v -> {
                controlButtonsLayout.removeView(pauseButtonsLayout);
                pauseButtonsLayout = null;
                startPauseBtn.setVisibility(View.VISIBLE);
                startTimer(timeLeft);
            });

            Button stopBtn = new Button(this);
            stopBtn.setText("STOP");
            stopBtn.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_dark));
            stopBtn.setTextColor(getResources().getColor(android.R.color.white));
            stopBtn.setOnClickListener(v -> {
                recordFocusTime((int) ((selectedMillis - timeLeft) / 60000));
                resetButtons();
                updateTimerDisplay(selectedMillis);
                controlButtonsLayout.removeView(pauseButtonsLayout);
                pauseButtonsLayout = null;
                startPauseBtn.setVisibility(View.VISIBLE);
            });

            pauseButtonsLayout.addView(continueBtn);
            pauseButtonsLayout.addView(stopBtn);
        }

        controlButtonsLayout.addView(pauseButtonsLayout);
    }

    private void resetButtons() {
        isRunning = false;
        isPaused = false;
        timeLeft = selectedMillis;
        startPauseBtn.setText("START");
    }

    private void updateTimerDisplay(long millis) {
        int minutes = (int) (millis / 60000);
        int seconds = (int) ((millis / 1000) % 60);
        timerText.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }

    private void recordFocusTime(int minutes) {
        String record = minutes + " minutes";
        String key = focusRef.push().getKey();
        if (key != null) {
            focusRef.child(key).setValue(record);
            focusHistory.add(record);
            updateRecordsUI();
        }
    }

    private void loadFocusRecords() {
        focusRef.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                focusHistory.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String record = snap.getValue(String.class);
                    if (record != null) focusHistory.add(record);
                }
                updateRecordsUI();
            }

            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(pomo.this, "Error loading records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecordsUI() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < focusHistory.size(); i++) {
            String record = focusHistory.get(i);
            sb.append(i + 1).append(". ").append(record).append("    ❌\n");
        }
        recordsText.setText(sb.toString());

        recordsText.setOnClickListener(v -> {
            if (!focusHistory.isEmpty()) {
                focusHistory.remove(focusHistory.size() - 1);
                updateRecordsUI();
                focusRef.setValue(focusHistory);
            }
        });
    }
}
