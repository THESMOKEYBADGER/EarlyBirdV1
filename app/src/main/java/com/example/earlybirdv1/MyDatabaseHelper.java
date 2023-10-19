package com.example.earlybirdv1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DATABASE_NAME = "MyAppDatabase.db";
    private static final int DATABASE_VERSION = 13; // Increment the version when you make changes to the database schema

    // Constructor
    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the user table
        db.execSQL("CREATE TABLE user (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "password TEXT NOT NULL)");

        // Create the bird_sightings table with a foreign key constraint
        db.execSQL("CREATE TABLE bird_sightings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "bird_name TEXT NOT NULL," +
                "sighting_time TEXT NOT NULL," +
                "user_latitude REAL NOT NULL," +  // Use REAL for latitude
                "user_longitude REAL NOT NULL," + // Use REAL for longitude
                "photo_path TEXT NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES user(id))");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database schema upgrades here
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS bird_sightings");
            db.execSQL("DROP TABLE IF EXISTS user");
            onCreate(db);
        }
    }

    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", user.getUsername());
        values.put("email", user.getEmail());
        values.put("password", user.getPassword());
        long newRowId = db.insert("user", null, values);
        db.close();
        return newRowId;
    }

    // Method to check if the user exists with the given credentials
    public boolean checkUserCredentials(String usernameOrEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the columns you want to retrieve
        String[] projection = {"username", "email", "password"};

        // Define the WHERE clause to check username or email and password
        String selection = "(username = ? OR email = ?) AND password = ?";
        String[] selectionArgs = {usernameOrEmail, usernameOrEmail, password};

        // Query the database
        Cursor cursor = db.query("user", projection, selection, selectionArgs, null, null, null);

        // Check if a user with the provided credentials exists
        boolean userExists = cursor.moveToFirst();

        // Close the cursor and database
        cursor.close();
        db.close();

        return userExists;
    }

    public long insertBirdSighting(BirdSighting birdSighting) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the provided userId exists in the user table
        if (isUserExists(birdSighting.getUserId())) {
            ContentValues values = new ContentValues();
            values.put("user_id", birdSighting.getUserId());
            values.put("bird_name", birdSighting.getBirdName());
            values.put("sighting_time", birdSighting.getSightingTime());
            values.put("user_latitude", birdSighting.getUserLatitude());
            values.put("user_longitude", birdSighting.getUserLongitude());
            values.put("photo_path", birdSighting.getPhotoPath());
            long newRowId = db.insert("bird_sightings", null, values);
            return newRowId; // Return newRowId before closing the database connection
        } else {
            // The provided userId does not exist in the user table
            return -1; // Indicate that the insertion failed
        }







    }

    private boolean isUserExists(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the columns you want to retrieve
        String[] projection = {"id"};

        // Define the WHERE clause to check if the user with the provided userId exists
        String selection = "id = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        // Query the user table
        Cursor cursor = db.query("user", projection, selection, selectionArgs, null, null, null);

        // Check if a user with the provided userId exists
        boolean userExists = cursor.moveToFirst();


        return userExists;
    }

    // Add this method to retrieve the userId based on username and password
    public String getUserId(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userId = null;

        String[] columns = { "id" }; // Change "userId" to "id"
        String selection = "username = ? AND password = ?";
        String[] selectionArgs = { username, password };

        Cursor cursor = db.query("user", columns, selection, selectionArgs, null, null, null); // Change "users" to "user"

        if (cursor != null && cursor.moveToFirst()) {
            int userIdColumnIndex = cursor.getColumnIndex("id"); // Change "userId" to "id"
            if (userIdColumnIndex != -1) {
                userId = cursor.getString(userIdColumnIndex);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return userId;
    }

    public List<BirdSighting> getUserSpecificSightings(String userId) {
        List<BirdSighting> sightings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the columns you want to retrieve
        String[] projection = {
                "bird_name",
                "sighting_time",
                "user_latitude",
                "user_longitude",
                "photo_path"
        };

        // Define the WHERE clause to filter sightings by user ID
        String selection = "user_id = ?";
        String[] selectionArgs = { userId };

        // Query the database
        Cursor cursor = db.query("bird_sightings", projection, selection, selectionArgs, null, null, null);

        // Check if the cursor has data
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int birdNameColumnIndex = cursor.getColumnIndex("bird_name");
                    int sightingTimeColumnIndex = cursor.getColumnIndex("sighting_time");
                    int userLatitudeColumnIndex = cursor.getColumnIndex("user_latitude");
                    int userLongitudeColumnIndex = cursor.getColumnIndex("user_longitude");
                    int photoPathColumnIndex = cursor.getColumnIndex("photo_path");

                    // Check if columns were found
                    if (birdNameColumnIndex != -1 &&
                            sightingTimeColumnIndex != -1 &&
                            userLatitudeColumnIndex != -1 &&
                            userLongitudeColumnIndex != -1 &&
                            photoPathColumnIndex != -1) {

                        String birdName = cursor.getString(birdNameColumnIndex);
                        String sightingTime = cursor.getString(sightingTimeColumnIndex);
                        double userLatitude = cursor.getDouble(userLatitudeColumnIndex);
                        double userLongitude = cursor.getDouble(userLongitudeColumnIndex);
                        String photoPath = cursor.getString(photoPathColumnIndex);

                        BirdSighting sighting = new BirdSighting(userId, birdName, sightingTime, userLatitude, userLongitude, photoPath);
                        sightings.add(sighting);
                    } else {
                        // Handle the case where a column was not found
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        db.close();

        return sightings;
    }







}
