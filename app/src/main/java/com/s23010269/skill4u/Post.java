package com.s23010269.skill4u;

public class Post {
    private String postId;
    private String username;
    private String text;
    private long timestamp;

    public Post() {
        // Needed for Firebase
    }

    public Post(String postId, String username, String text, long timestamp) {
        this.postId = postId;
        this.username = username;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters for all fields
    public String getPostId() {
        return postId;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }
}