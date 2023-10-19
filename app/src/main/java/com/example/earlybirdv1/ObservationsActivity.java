package com.example.earlybirdv1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ObservationsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SightingAdapter sightingAdapter;
    private List<BirdSighting> sightings;
    private String userId;  // Add a member variable to store the userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observations);

        // Retrieve the userId from the Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        recyclerView = findViewById(R.id.recyclerView);

        sightings = loadUserSpecificSightings();
        sightingAdapter = new SightingAdapter(this, sightings);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sightingAdapter);
    }

    private List<BirdSighting> loadUserSpecificSightings() {
        // Implement this method to retrieve user-specific sightings from your database
        // and return a List of BirdSighting objects.
        // For example, you can query your database here and fetch the sightings.
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(this);
        // Use the retrieved userId to load user-specific sightings
        List<BirdSighting> sightings = databaseHelper.getUserSpecificSightings(userId);

        // Update the sightings with the nearest road information
        for (BirdSighting sighting : sightings) {
            updateSightingWithNearestRoad(sighting, this);
        }

        return sightings;
    }

    private void updateSightingWithNearestRoad(BirdSighting sighting, Context context) {
        // Use the Geocoder to get the nearest road's name
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(sighting.getUserLatitude(), sighting.getUserLongitude(), 1);
            if (!addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                sighting.setNearestRoadName(address);
            } else {
                sighting.setNearestRoadName("Location not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            sighting.setNearestRoadName("Location not found");
        }
    }

    public void onButton1Click(View view) {
        // Create an Intent to go to the HomepageActivity
        Intent intent = new Intent(this, HomePageActivity.class);

        // Pass the userId as an extra in the intent
        intent.putExtra("userId", userId);

        startActivity(intent);
    }
}
