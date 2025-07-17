package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class home extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private TextView getName;
    private ImageView openMenu;
    private ImageView profile;
    private FloatingActionButton fab;
    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private List<Task> taskList = new ArrayList<>();

    private String currentUsername;
    private DatabaseReference usersRef;
    private DatabaseReference schedulesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // UI refs
        getName    = findViewById(R.id.get_name);
        openMenu   = findViewById(R.id.openmenu);
        profile    = findViewById(R.id.profile);
        fab        = findViewById(R.id.add_shedules);
        rvTasks    = findViewById(R.id.rv_tasks);

        // Firebase refs
        usersRef     = FirebaseDatabase.getInstance().getReference("Users");
        schedulesRef = FirebaseDatabase.getInstance().getReference("Schedules");

        // get username from Intent or prefs
        String username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            username = prefs.getString("USERNAME", null);
        }
        currentUsername = username;

        // load & display your name
        if (username != null) {
            usersRef.child(username).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            String name = snap.getValue(String.class);
                            getName.setText(name != null ? name.toUpperCase() : "USER");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError err) {
                            Log.e(TAG, "error reading name", err.toException());
                            getName.setText("USER");
                        }
                    });
        } else {
            getName.setText("GUEST");
        }

        // calendar UI
        setupCalendar();

        // setup RecyclerView
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList, task -> {
            // Handle task update or deletion here
            // e.g., show a simple confirmation dialog for delete or open a new task editing screen
            updateOrDeleteTask(task);
        });
        rvTasks.setAdapter(taskAdapter);

        // fetch initial tasks
        refreshTasks();

        // FAB opens add‑task dialog
        fab.setOnClickListener(v -> {
            AddTaskDialogFragment dlg = AddTaskDialogFragment.newInstance(currentUsername, this::refreshTasks);
            dlg.show(getSupportFragmentManager(), "AddTask");
        });

        openMenu.setOnClickListener(v -> {
            startActivity(new Intent(this, menu.class));
        });

        profile.setOnClickListener(v -> {
            Intent i = new Intent(this, profile.class);
            i.putExtra("USERNAME", currentUsername);
            startActivity(i);
        });
    }

    /** loads from Firebase, deletes expired, updates RecyclerView */
    private void refreshTasks() {
        if (currentUsername == null) return;
        schedulesRef.child(currentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snap) {
                        taskList.clear();
                        long now = System.currentTimeMillis();
                        for (DataSnapshot child : snap.getChildren()) {
                            Task t = child.getValue(Task.class);
                            if (t == null) continue;
                            // parse date+time into timestamp
                            long ts = parseDateTime(t.date, t.time);
                            if (ts < now) {
                                // expired → remove
                                schedulesRef.child(currentUsername).child(t.id).removeValue();
                            } else {
                                taskList.add(t);
                            }
                        }
                        // sort by timestamp ascending
                        Collections.sort(taskList, Comparator.comparingLong(
                                t -> parseDateTime(t.date, t.time)
                        ));
                        taskAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError err) {
                        Log.e(TAG, "err loading tasks", err.toException());
                    }
                });
    }

    /** helper to turn "2025-07-01" + "14:30" into millis */
    private long parseDateTime(String date, String time) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                    .parse(date + " " + time);
            return d != null ? d.getTime() : 0;
        } catch (ParseException e) {
            return 0;
        }
    }

    private void setupCalendar() {
        LinearLayout calendarLayout = findViewById(R.id.calendar_container);
        calendarLayout.removeAllViews();

        String[] daysOfWeek = {"M", "T", "W", "T", "F", "S", "S"};
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int offset = dow - Calendar.MONDAY;
        if (offset < 0) offset += 7;
        cal.add(Calendar.DATE, -offset);

        for (int i = 0; i < 7; i++) {
            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(dpToPx(48), dpToPx(64));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dayLayout.setLayoutParams(params);

            boolean isToday =
                    cal.get(Calendar.DAY_OF_WEEK) == Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            int date = cal.get(Calendar.DAY_OF_MONTH);

            dayLayout.setBackground(ContextCompat.getDrawable(this,
                    isToday ? R.drawable.day_active : R.drawable.day_inactive));

            TextView dayTv = new TextView(this);
            dayTv.setText(daysOfWeek[i]);
            dayTv.setTextColor(0xFFFFFFFF);
            dayTv.setTextSize(14f);
            dayTv.setTypeface(null, Typeface.BOLD); // Fixed Typeface usage
            dayTv.setGravity(Gravity.CENTER);

            TextView dateTv = new TextView(this);
            dateTv.setText(String.valueOf(date));
            dateTv.setTextColor(0xFFFFFFFF);
            dateTv.setTextSize(14f);
            dateTv.setGravity(Gravity.CENTER);

            dayLayout.addView(dayTv);
            dayLayout.addView(dateTv);
            calendarLayout.addView(dayLayout);

            cal.add(Calendar.DATE, 1);
        }
    }

    private void updateOrDeleteTask(Task task) {
        // Handle task update or deletion, can show a confirmation dialog
        // For example, for deleting a task:
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Task?")
                .setMessage("This cannot be undone.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    schedulesRef.child(currentUsername).child(task.id)
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Task deleted");
                                refreshTasks();
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to delete task", e));
                })
                .setNegativeButton("No", null)
                .show();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
