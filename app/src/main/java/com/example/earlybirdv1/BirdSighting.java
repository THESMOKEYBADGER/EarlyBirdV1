package com.example.earlybirdv1;

import com.google.firebase.firestore.PropertyName;

public class BirdSighting {
    @PropertyName("user_id")
    private String userId;

    @PropertyName("bird_name")
    private String birdName;

    @PropertyName("sighting_time")
    private String sightingTime;

    @PropertyName("user_latitude")
    private double userLatitude;

    @PropertyName("user_longitude")
    private double userLongitude;

    @PropertyName("photo_path")
    private String photoPath;

    @PropertyName("nearest_road_name")
    private String nearestRoadName;

    // No-argument constructor for Firestore deserialization
    public BirdSighting() {
        // Default constructor required for Firestore deserialization
    }

    public BirdSighting(String userId, String birdName, String sightingTime, double userLatitude, double userLongitude, String photoPath) {
        this.userId = userId;
        this.birdName = birdName;
        this.sightingTime = sightingTime;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.photoPath = photoPath;
    }

    @PropertyName("user_id")
    public String getUserId() {
        return userId;
    }

    @PropertyName("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("bird_name")
    public String getBirdName() {
        return birdName;
    }

    @PropertyName("bird_name")
    public void setBirdName(String birdName) {
        this.birdName = birdName;
    }

    @PropertyName("sighting_time")
    public String getSightingTime() {
        return sightingTime;
    }

    @PropertyName("sighting_time")
    public void setSightingTime(String sightingTime) {
        this.sightingTime = sightingTime;
    }

    @PropertyName("user_latitude")
    public double getUserLatitude() {
        return userLatitude;
    }

    @PropertyName("user_latitude")
    public void setUserLatitude(double userLatitude) {
        this.userLatitude = userLatitude;
    }

    @PropertyName("user_longitude")
    public double getUserLongitude() {
        return userLongitude;
    }

    @PropertyName("user_longitude")
    public void setUserLongitude(double userLongitude) {
        this.userLongitude = userLongitude;
    }

    @PropertyName("photo_path")
    public String getPhotoPath() {
        return photoPath;
    }

    @PropertyName("photo_path")
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    @PropertyName("nearest_road_name")
    public String getNearestRoadName() {
        return nearestRoadName;
    }

    @PropertyName("nearest_road_name")
    public void setNearestRoadName(String nearestRoadName) {
        this.nearestRoadName = nearestRoadName;
    }
}
