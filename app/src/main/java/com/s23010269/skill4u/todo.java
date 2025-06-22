package com.s23010269.skill4u;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class todo extends AppCompatActivity {

    private ImageView openMenu;

    Button createBtn;
    LinearLayout allListContainer, todayListContainer;
    ArrayList<TodoItem> todoItems;
    TextView allTab, todayTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo);

        createBtn = findViewById(R.id.createtodo);
        openMenu = findViewById(R.id.openmenu);
        allListContainer = findViewById(R.id.all_list_container);
        todayListContainer = findViewById(R.id.today_list_container);
        allTab = findViewById(R.id.all_list);
        todayTab = findViewById(R.id.friend_request);

        todoItems = new ArrayList<>();

        // Show ALL by default
        showAllList();

        createBtn.setOnClickListener(v -> showCreateTodoPopup());

        allTab.setOnClickListener(v -> showAllList());
        todayTab.setOnClickListener(v -> showTodayList());

        openMenu.setOnClickListener(view -> {
            Intent intent = new Intent(todo.this, menu.class);
            startActivity(intent);
        });
    }

    private void showAllList() {
        allListContainer.setVisibility(View.VISIBLE);
        todayListContainer.setVisibility(View.GONE);
        highlightTab(allTab, todayTab);
    }

    private void showTodayList() {
        allListContainer.setVisibility(View.GONE);
        todayListContainer.setVisibility(View.VISIBLE);
        highlightTab(todayTab, allTab);
    }

    private void highlightTab(TextView selected, TextView unselected) {
        selected.setBackgroundResource(R.drawable.selected_tab_bg);
        selected.setTextColor(getResources().getColor(android.R.color.white));
        unselected.setBackgroundResource(0);
        unselected.setTextColor(getResources().getColor(android.R.color.white));
    }

    private void showCreateTodoPopup() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_create_todo, null);

        EditText titleInput = popupView.findViewById(R.id.editTextTitle);
        EditText descriptionInput = popupView.findViewById(R.id.editTextDescription);
        TextView dateText = popupView.findViewById(R.id.textViewSelectedDate);
        TextView timeText = popupView.findViewById(R.id.textViewSelectedTime);
        Button saveBtn = popupView.findViewById(R.id.buttonSaveTodo);

        final Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dateText.setText(dateFormat.format(calendar.getTime()));
        timeText.setText(timeFormat.format(calendar.getTime()));

        dateText.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(todo.this,
                    (view, y, m, d) -> {
                        calendar.set(y, m, d);
                        dateText.setText(dateFormat.format(calendar.getTime()));
                    }, year, month, day).show();
        });

        timeText.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            new TimePickerDialog(todo.this,
                    (view, h, min) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, h);
                        calendar.set(Calendar.MINUTE, min);
                        timeText.setText(timeFormat.format(calendar.getTime()));
                    }, hour, minute, true).show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(popupView)
                .setCancelable(true)
                .create();

        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String date = dateText.getText().toString();
            String time = timeText.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(todo.this, "Please enter a title", Toast.LENGTH_SHORT).show();
                return;
            }

            TodoItem newItem = new TodoItem(title, description, date, time);
            todoItems.add(newItem);
            addTodoToViews(newItem);

            Toast.makeText(todo.this, "Todo saved!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void addTodoToViews(TodoItem item) {
        TextView todoView = new TextView(this);
        todoView.setText(item.title + " - " + item.date + " " + item.time);
        todoView.setPadding(16, 16, 16, 16);
        todoView.setTextColor(getResources().getColor(android.R.color.white));
        todoView.setBackgroundResource(R.drawable.todo_item_background);

        LinearLayout todoLayout = new LinearLayout(this);
        todoLayout.setOrientation(LinearLayout.HORIZONTAL);
        todoLayout.setPadding(8, 8, 8, 8);
        todoLayout.setBackgroundResource(R.drawable.todo_item_background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        todoView.setLayoutParams(params);

        Button completeBtn = new Button(this);
        completeBtn.setText("Complete");

        todoLayout.addView(todoView);
        todoLayout.addView(completeBtn);

        todoView.setOnClickListener(v -> showTodoDetailsPopup(item));

        completeBtn.setOnClickListener(v -> {
            todoItems.remove(item);
            allListContainer.removeView(todoLayout);
            todayListContainer.removeView(todoLayout);
        });

        if (isToday(item.date)) {
            todayListContainer.addView(todoLayout);
        } else {
            allListContainer.addView(todoLayout);
        }
    }

    private void showTodoDetailsPopup(TodoItem item) {
        new AlertDialog.Builder(this)
                .setTitle(item.title)
                .setMessage("Description:\n" + item.description + "\n\nDate: " + item.date + "\nTime: " + item.time)
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean isToday(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = dateFormat.format(Calendar.getInstance().getTime());
        return todayStr.equals(dateStr);
    }

    static class TodoItem {
        String title;
        String description;
        String date;
        String time;

        TodoItem(String title, String description, String date, String time) {
            this.title = title;
            this.description = description;
            this.date = date;
            this.time = time;
        }
    }
}
