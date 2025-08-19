// File: app/src/main/java/com/s23010269/skill4u/Challenge.java
package com.s23010269.skill4u; // Package changed

import java.io.Serializable; // Added for passing Challenge objects via Bundle

public class Challenge implements Serializable { // Implemented Serializable
    private String id;
    private String title;
    private String difficulty;
    private String description; // Added description field
    private String relatedLinks; // Added relatedLinks field
    private int usersJoined;
    private int points; // New: Points for completing the challenge
    private boolean isJoined; // New: To track if the user has joined
    private boolean isCompleted; // New: To track if the user has completed
    private String completionDate; // New field to store completion date

    // Required empty constructor for Firebase
    public Challenge() {
    }

    public Challenge(String id, String title, String difficulty, String description, String relatedLinks, int usersJoined, int points, boolean isJoined, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.difficulty = difficulty;
        this.description = description;
        this.relatedLinks = relatedLinks; // Initialized relatedLinks
        this.usersJoined = usersJoined;
        this.points = points; // Initialized points
        this.isJoined = isJoined; // Initialized isJoined
        this.isCompleted = isCompleted; // Initialized isCompleted
        this.completionDate = completionDate; // Initialize completionDate
    }

    // Getters
    public String getCompletionDate() { // Getter for completionDate
        return completionDate;
    }

    public void setCompletionDate(String completionDate) { // Setter for completionDate
        this.completionDate = completionDate;
    }
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
        return description;
    }

    public String getRelatedLinks() { // Getter for relatedLinks
        return relatedLinks;
    }

    public int getUsersJoined() {
        return usersJoined;
    }

    public int getPoints() { // Getter for points
        return points;
    }

    public boolean isJoined() { // Getter for isJoined
        return isJoined;
    }

    public boolean isCompleted() { // Getter for isCompleted
        return isCompleted;
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
        this.description = description;
    }

    public void setRelatedLinks(String relatedLinks) { // Setter for relatedLinks
        this.relatedLinks = relatedLinks;
    }

    public void setUsersJoined(int usersJoined) {
        this.usersJoined = usersJoined;
    }

    public void setPoints(int points) { // Setter for points
        this.points = points;
    }

    public void setJoined(boolean joined) { // Setter for isJoined
        isJoined = joined;
    }

    public void setCompleted(boolean completed) { // Setter for isCompleted
        isCompleted = completed;
    }
}