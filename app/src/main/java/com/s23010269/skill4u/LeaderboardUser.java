package com.s23010269.skill4u;

public class LeaderboardUser {
    private String name;
    private int totalPoints;

    public LeaderboardUser() {
        // Required for Firebase
    }

    public LeaderboardUser(String name, int totalPoints) {
        this.name = name;
        this.totalPoints = totalPoints;
    }

    public String getName() {
        return name;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
