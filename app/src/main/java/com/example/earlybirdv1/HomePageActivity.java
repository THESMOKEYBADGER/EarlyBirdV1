package com.example.earlybirdv1;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.android.volley.VolleyError;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import android.content.Context;
import android.content.SharedPreferences;
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
        logSightingActivity = new LogSightingActivity(this, userId);

        // Set up the SeekBar listener
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sliderValue.setText(progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Handle when the user starts moving the slider
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Handle when the user stops moving the slider
                if (currentLocation != null) {
                    // Fetch and plot bird data after the user stops moving the slider
                    fetchBirdDataAndPlotPoints(slider.getProgress(), currentLocation.getLatitude(), currentLocation.getLongitude());
                }
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
        } else {
            Toast.makeText(this, "Location permission is denied, please allow the permission", Toast.LENGTH_SHORT).show();
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
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (selectedMarker != null && selectedMarker.equals(marker)) {
                    marker.hideInfoWindow();
                    // The same marker was clicked again, hide the route and show all markers
                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    selectedMarker = null;
                    currentRoute.remove();
                    currentRoute = null;

                    showAllMarkers();

                } else {
                    if (selectedMarker != null) {
                        // Another marker was previously selected, hide it and its route
                        selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        selectedMarker = null;
                        currentRoute.remove();
                        currentRoute = null;
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

        try {
            DirectionsResult result = directions.origin(origin)
                    .destination(destination)
                    .mode(TravelMode.DRIVING)
                    .await();

            List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(result.routes[0].overviewPolyline.getEncodedPath());
            List<LatLng> androidLatLngs = new ArrayList<>();

            for (com.google.maps.model.LatLng decodedLatLng : decodedPath) {
                androidLatLngs.add(new LatLng(decodedLatLng.lat, decodedLatLng.lng));
            }

            currentRoute = mMap.addPolyline(new PolylineOptions()
                    .addAll(androidLatLngs)
                    .width(10)
                    .color(Color.BLUE));

        } catch (Exception e) {
            e.printStackTrace();
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

}
