package com.example.earlybirdv1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogSightingActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private EditText birdNameEditText;
    private TextView timeTextView;
    private ImageButton uploadImageButton;
    private ImageButton saveSightingButton;
    private String photoPath;
    private String userId;
    private double userLatitude;
    private double userLongitude;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private final HomePageActivity homePageActivity;
    private MyDatabaseHelper dbHelper;

    public LogSightingActivity(HomePageActivity homePageActivity, String userId, MyDatabaseHelper dbHelper) {
        this.homePageActivity = homePageActivity;
        this.userId = userId;
        this.dbHelper = dbHelper;

        imagePickerLauncher = homePageActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData();
                            if (selectedImageUri != null) {
                                Log.d("LogSightingActivity", "Selected Image URI: " + selectedImageUri.toString());
                                photoPath = selectedImageUri.toString();
                                Log.d("LogSightingActivity", "Image URI: " + photoPath);
                            }
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_sighting);

        birdNameEditText = findViewById(R.id.editTextText);
        timeTextView = findViewById(R.id.time);
        uploadImageButton = findViewById(R.id.uploadImage);
        saveSightingButton = findViewById(R.id.saveSighting);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        timeTextView.setText(dateFormat.format(date));

        dbHelper = new MyDatabaseHelper(this);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri selectedImageUri = data.getData();
                            if (selectedImageUri != null) {
                                Log.d("LogSightingActivity", "Selected Image URI: " + selectedImageUri.toString());
                                photoPath = selectedImageUri.toString();
                                Log.d("LogSightingActivity", "Image URI: " + photoPath);
                            }
                        }
                    }
                });

        uploadImageButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(LogSightingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });

        saveSightingButton.setOnClickListener(v -> {
            String birdName = birdNameEditText.getText().toString();
            String sightingTime = timeTextView.getText().toString();

            if (birdName.isEmpty() || photoPath == null) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            } else {
                userId = dbHelper.getCurrentUserId();

                if (userId != null) {
                    dbHelper.getUserSpecificSightings(userId, new OnCompleteListener<List<BirdSighting>>() {
                        @Override
                        public void onComplete(Task<List<BirdSighting>> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                if (!task.getResult().isEmpty()) {
                                    BirdSighting lastSighting = task.getResult().get(0);
                                    userLatitude = lastSighting.getUserLatitude();
                                    userLongitude = lastSighting.getUserLongitude();
                                }

                                BirdSighting birdSighting = new BirdSighting(userId, birdName, sightingTime, userLatitude, userLongitude, photoPath);
                                dbHelper.insertBirdSighting(birdSighting, new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LogSightingActivity.this, "Sighting saved!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Log.e("LogSightingActivity", "Error inserting bird sighting", task.getException());
                                            Toast.makeText(LogSightingActivity.this, "Error saving the sighting.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Log.w("LogSightingActivity", "Error getting user-specific sightings");
                            }
                        }
                    });
                } else {
                    Log.w("LogSightingActivity", "User ID is null");
                }
            }

            Log.d("LogSightingActivity", "userID " + userId);
            Log.d("LogSightingActivity", "BirdName" + birdName);
            Log.d("LogSightingActivity", "dateTime" + sightingTime);
        });
    }

    public void showLogSightingInDialog(BottomSheetDialog dialog, Location currentLocation, String userId) {
        dialog.show();
        birdNameEditText = dialog.findViewById(R.id.editTextText);
        timeTextView = dialog.findViewById(R.id.time);
        uploadImageButton = dialog.findViewById(R.id.uploadImage);
        saveSightingButton = dialog.findViewById(R.id.saveSighting);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        timeTextView.setText(dateFormat.format(date));

        uploadImageButton.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(homePageActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(homePageActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            } else {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            }
        });

        saveSightingButton.setOnClickListener(v -> {
            String birdName = birdNameEditText.getText().toString();
            String sightingTime = timeTextView.getText().toString();

            if (birdName.isEmpty() || photoPath == null) {
                Toast.makeText(homePageActivity, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            } else {
                userLatitude = currentLocation.getLatitude();
                userLongitude = currentLocation.getLongitude();

                BirdSighting birdSighting = new BirdSighting(userId, birdName, sightingTime, userLatitude, userLongitude, photoPath);

                dbHelper.insertBirdSighting(birdSighting, new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(homePageActivity, "Sighting saved!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Log.e("LogSightingActivity", "Error inserting bird sighting", task.getException());
                            Toast.makeText(homePageActivity, "Error saving the sighting.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            Log.d("LogSightingActivity", "location" + currentLocation);
            Log.d("LogSightingActivity", "userID " + userId);
            Log.d("LogSightingActivity", "BirdName" + birdName);
            Log.d("LogSightingActivity", "dateTime" + sightingTime);
        });
    }
}
