package com.example.earlybirdv1;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ObservationsActivity extends AppCompatActivity {
    private static final int REQUEST_OPEN_DOCUMENT = 2;

    private RecyclerView recyclerView;
    private SightingAdapter sightingAdapter;
    private String userId;  // Add a member variable to store the userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observations);

        // Retrieve the userId from the Intent
        Intent intent = getIntent();
        userId = intent.getStringExtra("userId");

        recyclerView = findViewById(R.id.recyclerView);

        loadUserSpecificSightings(new OnCompleteListener<List<BirdSighting>>() {
            @Override
            public void onComplete(Task<List<BirdSighting>> task) {
                if (task.isSuccessful()) {
                    List<BirdSighting> sightings = task.getResult();
                    updateSightingsWithNearestRoad(sightings, ObservationsActivity.this);

                    sightingAdapter = new SightingAdapter(ObservationsActivity.this, sightings);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ObservationsActivity.this));
                    recyclerView.setAdapter(sightingAdapter);

                    // Set the item click listener for handling clicks on sightings
                    sightingAdapter.setOnItemClickListener(new SightingAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(BirdSighting sighting) {
                            // Handle click on the entire item (if needed)
                            // You can open a detailed view, for example
                        }

                        @Override
                        public void onImagePreviewClick(String imageUrl) {
                            // Open the document using ACTION_OPEN_DOCUMENT
                            openDocument(imageUrl);
                        }
                    });
                } else {
                    Log.w("ObservationsActivity", "Error loading sightings", task.getException());
                }
            }
        });
    }

    private void loadUserSpecificSightings(OnCompleteListener<List<BirdSighting>> listener) {
        MyDatabaseHelper databaseHelper = new MyDatabaseHelper(this);
        databaseHelper.getUserSpecificSightings(userId, listener);
    }

    private void updateSightingsWithNearestRoad(List<BirdSighting> sightings, ObservationsActivity context) {
        for (BirdSighting sighting : sightings) {
            updateSightingWithNearestRoad(sighting, context);
        }
    }

    private void updateSightingWithNearestRoad(BirdSighting sighting, ObservationsActivity context) {
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

    private void openDocument(String imageUrl) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_OPEN_DOCUMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_DOCUMENT && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                String selectedImageUri = data.getData().toString();
                showImageBottomSheetDialog(selectedImageUri);
            }
        }
    }

    private void showImageBottomSheetDialog(String imageUrl) {
        ImagePreviewBottomSheetFragment bottomSheetFragment = new ImagePreviewBottomSheetFragment(imageUrl);
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }
}
