// File: app/src/main/java/com/s23010269/skill4u/models/Challenge.java
package com.s23010269.skill4u; // Changed package to 'models'

public class Challenge { // Renamed class to 'Challenge' (capital C)
    private String id;
    private String title;
    private String difficulty;
    private String description; // Added description field
    private int usersJoined;

    // Required empty constructor for Firebase
    public Challenge() {
    }

    public Challenge(String id, String title, String difficulty, String description, int usersJoined) {
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.description = description; // Initialized description
        this.usersJoined = usersJoined;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getDescription() {
        return description; // Getter for description
    }

    public int getUsersJoined() {
        return usersJoined;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public void setDescription(String description) {
        this.description = description; // Setter for description
    }

    public void setUsersJoined(int usersJoined) {
        this.usersJoined = usersJoined;
    }
}