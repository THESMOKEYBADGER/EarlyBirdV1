package com.example.earlybirdv1;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class LogSightingActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private EditText birdNameEditText;
    private TextView timeTextView;
    private ImageButton uploadImageButton;
    private ImageButton saveSightingButton;
    private String photoPath;
    private String userId; // User ID passed from HomePageActivity
    private double userLatitude;
    private double userLongitude;
    private final ActivityResultLauncher<Intent> imagePickerLauncher;
    private final HomePageActivity homePageActivity;

    public LogSightingActivity(HomePageActivity homePageActivity, String userId) {
        this.homePageActivity = homePageActivity;
        this.userId = userId; // Set the user ID when the user logs in
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

                MyDatabaseHelper dbHelper = new MyDatabaseHelper(homePageActivity);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                BirdSighting birdSighting = new BirdSighting(this.userId, birdName, sightingTime, userLatitude, userLongitude, photoPath);
                long newRowId = dbHelper.insertBirdSighting(birdSighting);

                if (newRowId != -1) {
                    Toast.makeText(homePageActivity, "Sighting saved!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(homePageActivity, "Error saving the sighting.", Toast.LENGTH_SHORT).show();
                }
            }

            Log.d("LogSightingActivity", "location" + currentLocation);
            Log.d("LogSightingActivity", "userID " + userId);
            Log.d("LogSightingActivity", "BirdName" + birdName);
            Log.d("LogSightingActivity", "dateTime"+ sightingTime);
        });
    }
}
