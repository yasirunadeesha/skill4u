package com.s23010269.skill4u;

public class ChallengeItem {
    private String title;
    private String description;
    private boolean isCompleted;

    public ChallengeItem(String title, String description) {
        this.title = title;
        this.description = description;
        this.isCompleted = false;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    // Setters
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
