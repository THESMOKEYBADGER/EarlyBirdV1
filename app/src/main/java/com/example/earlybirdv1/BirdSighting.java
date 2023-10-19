package com.example.earlybirdv1;

public class BirdSighting {
    private String userId;
    private String birdName;
    private String sightingTime;
    private double userLatitude;   // Change this to store latitude
    private double userLongitude;  // Change this to store longitude
    private String photoPath;

    private String nearestRoadName;

    public BirdSighting(String userId, String birdName, String sightingTime, double userLatitude, double userLongitude, String photoPath) {
        this.userId = userId;
        this.birdName = birdName;
        this.sightingTime = sightingTime;
        this.userLatitude = userLatitude;     // Store latitude
        this.userLongitude = userLongitude;   // Store longitude
        this.photoPath = photoPath;
        this.nearestRoadName = nearestRoadName;
    }

    public String getUserId() {
        return userId;
    }

    public String getBirdName() {
        return birdName;
    }

    public String getSightingTime() {
        return sightingTime;
    }

    public double getUserLatitude() {   // Getter for latitude
        return userLatitude;
    }

    public double getUserLongitude() {  // Getter for longitude
        return userLongitude;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    // Include getters and setters for the new field
    public String getNearestRoadName() {
        return nearestRoadName;
    }

    public void setNearestRoadName(String nearestRoadName) {
        this.nearestRoadName = nearestRoadName;
    }
}