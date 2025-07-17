package com.s23010269.skill4u;

import java.io.Serializable;

public class Task implements Serializable {
    public String id;
    public String title;
    public String description;
    public String category;
    public String date;
    public String time;
    public boolean notify;
    public String username;

    // Required empty constructor for Firebase
    public Task() {}

    // Optional: Constructor for convenience
    public Task(String id, String title, String description, String category,
                String date, String time, boolean notify, String username) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.date = date;
        this.time = time;
        this.notify = notify;
        this.username = username;
    }
}
