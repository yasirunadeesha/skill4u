package com.s23010269.skill4u;

public class UserLocation {
    private String username;
    private double latitude;
    private double longitude;

    public UserLocation() {} // Firebase needs this

    public UserLocation(String username, double latitude, double longitude) {
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUsername() { return username; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setUsername(String username) { this.username = username; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
