package com.example.earlybirdv1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.android.volley.VolleyError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final int FINE_PERMISSION_CODE = 1;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SeekBar slider;
    private TextView sliderValue;
    private BottomSheetDialog bottomSheetDialog;
    private LogSightingActivity logSightingActivity;
    private Marker selectedMarker; // Store the selected marker
    private Polyline currentRoute; // Store the current route
    private List<Marker> birdMarkers = new ArrayList<>(); // Store all bird markers
    private static final String PREFS_NAME = "MyPrefs";
    private static final String NOTIFICATION_SHOWN = "notificationShown";

    public HomePageActivity() {
        // Default constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        // Check if the notification has been shown before
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean notificationShown = preferences.getBoolean(NOTIFICATION_SHOWN, false);

        // Create a notification dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View notificationView = getLayoutInflater().inflate(R.layout.notification_layout, null);
        builder.setView(notificationView);
        AlertDialog notificationDialog = builder.create();
        notificationDialog.setCancelable(false); // Prevent users from dismissing it by clicking outside

        // Handle the close button click
        Button closeButton = notificationView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationDialog.dismiss(); // Close the notification dialog
            }
        });

        // Show the notification dialog
        notificationDialog.show();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        slider = findViewById(R.id.slider);
        sliderValue = findViewById(R.id.sliderValue);

        // Initialize the BottomSheetDialog
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.activity_log_sighting);

        // Initialize the LogSightingActivity
        logSightingActivity = new LogSightingActivity(this, userId, new MyDatabaseHelper(this));

        // Set up the SeekBar listener
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sliderValue.setText(progress + " km");
                if (currentLocation != null) {
                    // Fetch and plot bird data after the user stops moving the slider
                    fetchBirdDataAndPlotPoints(slider.getProgress(), currentLocation.getLatitude(), currentLocation.getLongitude());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle when the user starts moving the slider
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle when the user stops moving the slider
            }
        });

        Button logSightingButton = findViewById(R.id.logSightingButton);

        // Find the "Log a Sighting" button
        logSightingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the currentLocation to LogSightingActivity and show the content in a bottom sheet dialog
                logSightingActivity.showLogSightingInDialog(bottomSheetDialog, currentLocation, userId);
            }
        });

        getLastLocation();

        Button observationsButton = findViewById(R.id.observationsButton);

        observationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start the ObservationsActivity
                Intent observationsIntent = new Intent(HomePageActivity.this, ObservationsActivity.class);

                // Pass the userId to ObservationsActivity
                observationsIntent.putExtra("userId", userId);

                // Start the ObservationsActivity
                startActivity(observationsIntent);
            }
        });
    }


    private void fetchBirdDataAndPlotPoints(int distance, double lat, double lng) {
        // Clear previous markers and reset selectedMarker
        if (selectedMarker != null) {
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            selectedMarker = null;
        }
        mMap.clear();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Fetch bird data
            FetchBirdData birdDataFetcher = new FetchBirdData(HomePageActivity.this);
            birdDataFetcher.fetchBirdData(lat, lng, distance, new FetchBirdData.BirdDataListener() {
                @Override
                public void onSuccess(double latitude, double longitude, String name) {
                    // Plot the bird data on the map
                    LatLng birdLocation = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(birdLocation)
                            .title(name);
                    Marker marker = mMap.addMarker(markerOptions);
                    birdMarkers.add(marker); // Add the marker to the list
                }

                @Override
                public void onError(VolleyError error) {
                    Log.e("MyApp", "API request error: " + error.getMessage());
                    // Handle API request failure
                }
            });

            // Fetch user-specific bird sightings
            fetchUserObservationsAndPlotPoints(distance, lat, lng);
        } else {
            Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchUserObservationsAndPlotPoints(int distance, double lat, double lng) {
        // Fetch user-specific bird sightings
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(this);
        String userId = databaseHelper.getCurrentUserId();

        if (userId != null) {
            databaseHelper.getUserSpecificSightings(userId, new OnCompleteListener<List<BirdSighting>>() {
                @Override
                public void onComplete(Task<List<BirdSighting>> task) {
                    if (task.isSuccessful()) {
                        List<BirdSighting> userObservations = task.getResult();

                        // Plot user-specific bird sightings on the map
                        plotUserObservationsOnMap(userObservations);
                    } else {
                        Log.w("MyApp", "Error loading user-specific sightings", task.getException());
                    }
                }
            });
        }
    }

    private void plotUserObservationsOnMap(List<BirdSighting> userObservations) {
        for (BirdSighting observation : userObservations) {
            LatLng observationLocation = new LatLng(observation.getUserLatitude(), observation.getUserLongitude());
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(observationLocation)
                    .title(observation.getBirdName())  // Assuming birdName is stored in the BirdSighting class
                    .snippet(observation.getSightingTime())  // Add relevant details here
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));  // Set marker color to orange
            Marker observationMarker = mMap.addMarker(markerOptions);
            // Customize the marker further if needed
            // observationMarker.setIcon(...);
        }
    }



    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<Location> task = fusedLocationProviderClient.getLastLocation();
            task.addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    updateLocationInURL(currentLocation.getLatitude(), currentLocation.getLongitude());
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(HomePageActivity.this);
                    updateMapWithNewLocation(currentLocation);
                }
            });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        }
    }

    private void updateMapWithNewLocation(Location newLocation) {
        if (mMap != null) {
            LatLng myLocation = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());

            Log.d("MyApp", "New location: " + myLocation.latitude + ", " + myLocation.longitude);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));

            Log.d("MyApp", "Camera moved to new location.");

            fetchBirdDataAndPlotPoints(slider.getProgress(), myLocation.latitude, myLocation.longitude);
        }
    }

    private void updateLocationInURL(double lat, double lng) {
        String updatedUrl = "https://api.ebird.org/v2/ref/hotspot/geo?lat=" + lat + "&lng=" + lng + "&dist=" + slider.getProgress();
        Log.d("MyApp", "Updated URL: " + updatedUrl);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (currentLocation != null) {
            LatLng myLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
            }
            fetchBirdDataAndPlotPoints(slider.getProgress(), currentLocation.getLatitude(), currentLocation.getLongitude());
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @SuppressLint("PotentialBehaviorOverride")
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (selectedMarker != null && selectedMarker.equals(marker)) {
                    marker.hideInfoWindow();
                    // The same marker was clicked again, hide the route and show all markers
                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    selectedMarker = null;
                    if (currentRoute != null) {
                        currentRoute.remove();
                        currentRoute = null;
                    }

                    showAllMarkers();

                } else {
                    if (selectedMarker != null) {
                        // Another marker was previously selected, hide it and its route
                        selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        selectedMarker = null;
                        if (currentRoute != null) {
                            currentRoute.remove();
                            currentRoute = null;
                        }
                        marker.hideInfoWindow();
                    }

                    // Show the route to the selected marker
                    selectedMarker = marker;
                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    LatLng destination = marker.getPosition();
                    getDirectionsToMarker(destination);
                    hideAllMarkersExceptSelected(selectedMarker);
                    marker.showInfoWindow();
                }
                return true;
            }
        });

    }

    // Modify the getDirectionsToMarker method
    private void getDirectionsToMarker(LatLng markerDestination) {
        if (currentLocation == null) {
            return;
        }

        if (currentRoute != null) {
            currentRoute.remove(); // Remove the previous route
        }

        com.google.maps.model.LatLng origin = new com.google.maps.model.LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(markerDestination.latitude, markerDestination.longitude);

        DirectionsApiRequest directions = new DirectionsApiRequest(new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_api_key))
                .build());
        HomePageActivity currentRef = this;

        directions.origin(origin)
                .destination(destination)
                .mode(TravelMode.DRIVING)
                .setCallback(new PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        handleDirectionsResult(result);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(HomePageActivity.this, "Error retrieving directions", Toast.LENGTH_SHORT).show();
                        });
                        // Handle failure, e.g., show an error message
                    }
                });
    }

    // Add a new method to handle the result
    private void handleDirectionsResult(DirectionsResult result) {
        if (result != null && result.routes != null && result.routes.length > 0) {
            String distanceText = result.routes[0].legs[0].distance.humanReadable;
            String durationText = result.routes[0].legs[0].duration.humanReadable;


            List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(result.routes[0].overviewPolyline.getEncodedPath());
            List<LatLng> androidLatLngs = new ArrayList<>();

            for (com.google.maps.model.LatLng decodedLatLng : decodedPath) {
                androidLatLngs.add(new LatLng(decodedLatLng.lat, decodedLatLng.lng));
            }

            runOnUiThread(() -> {
                currentRoute = mMap.addPolyline(new PolylineOptions()
                        .addAll(androidLatLngs)
                        .width(10)
                        .color(Color.BLUE));
                Toast.makeText(this, "Distance: " + distanceText + "\nDuration: " + durationText, Toast.LENGTH_LONG).show();

            });
        } else {
            // Handle the case where no routes are found
            Toast.makeText(this, "Unable to retrieve route information", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAllMarkers() {
        // Iterate through all markers on the map and set them to be visible
        for (Marker marker : birdMarkers) {
            marker.setVisible(true);
        }
    }

    private void hideAllMarkersExceptSelected(Marker selectedMarker) {
        // Iterate through all markers on the map and hide them except for the selected marker
        for (Marker marker : birdMarkers) {
            if (!marker.equals(selectedMarker)) {
                marker.setVisible(false);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
