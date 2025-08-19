package com.s23010269.skill4u;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;


public class AddTaskDialogFragment extends DialogFragment {

    private static final String ARG_USERNAME = "arg_username"; // Key for passing username
    private String username;
    private Runnable onSavedCallback; // Callback after saving task

    public static AddTaskDialogFragment newInstance(String username, Runnable onSaved) {
        AddTaskDialogFragment frag = new AddTaskDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USERNAME, username);
        frag.setArguments(args);
        frag.setOnSavedCallback(onSaved);
        return frag;
    }

    public void setOnSavedCallback(Runnable callback) {
        this.onSavedCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString(ARG_USERNAME); // Retrieve username
        }
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_add_task, container, false);

        // UI elements
        EditText etTitle       = view.findViewById(R.id.et_title);
        EditText etDesc        = view.findViewById(R.id.et_description);
        TextView tvCategory    = view.findViewById(R.id.tv_category);
        TextView tvDate        = view.findViewById(R.id.tv_date);
        TextView tvTime        = view.findViewById(R.id.tv_time);
        Switch swNotify        = view.findViewById(R.id.sw_notify);
        View   btnClose        = view.findViewById(R.id.btn_close);
        View   btnSave         = view.findViewById(R.id.btn_save);

        final String[] selectedCategory = {null}; // Store selected category

        // Category picker
        tvCategory.setOnClickListener(v -> {
            CategoryPickerDialogFragment categoryDialog =
                    new CategoryPickerDialogFragment(cat -> {
                        selectedCategory[0] = cat;
                        tvCategory.setText(cat);
                    });
            categoryDialog.show(getParentFragmentManager(), "pick_category");
        });

        // Date picker
        tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(),
                    (dp, year, month, dayOfMonth) -> {
                        String dateStr = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        tvDate.setText(dateStr);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time picker
        tvTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(requireContext(),
                    (tp, hourOfDay, minute) -> {
                        String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                        tvTime.setText(timeStr);
                    },
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE),
                    true).show();
        });

        // Close button dismisses dialog
        btnClose.setOnClickListener(v -> dismiss());

        // Save task button
        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String cat = selectedCategory[0];
            String date = tvDate.getText().toString().trim();
            String time = tvTime.getText().toString().trim();
            boolean notify = swNotify.isChecked();

            // Check required fields
            if (title.isEmpty() || cat == null || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate unique key for new task
            String key = FirebaseDatabase.getInstance()
                    .getReference("Schedules")
                    .child(username)
                    .push()
                    .getKey();

            // Create Task object
            Task task = new Task(key, title, desc, cat, date, time, notify, username);

            // Save task to Firebase
            FirebaseDatabase.getInstance()
                    .getReference("Schedules")
                    .child(username)
                    .child(key)
                    .setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Task saved", Toast.LENGTH_SHORT).show();
                        dismiss(); // Close dialog
                        if (onSavedCallback != null) onSavedCallback.run(); // Run callback
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(requireContext(), "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            // Make dialog full screen
            d.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }
    }
}
