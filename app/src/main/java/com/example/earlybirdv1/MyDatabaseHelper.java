package com.example.earlybirdv1;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyDatabaseHelper {

    private static final String TAG = "MyDatabaseHelper";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;

    public MyDatabaseHelper(Context context) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void insertUser(User user, OnCompleteListener<Void> listener) {
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task != null) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("username", user.getUsername());
                                userMap.put("email", user.getEmail());

                                db.collection("users").document(userId)
                                        .set(userMap)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1 != null) {
                                                if (task1.isSuccessful()) {
                                                    Log.d(TAG, "User added to Firestore");
                                                    listener.onComplete(task1);
                                                } else {
                                                    Log.w(TAG, "Error adding user to Firestore: " + task1.getException());
                                                    listener.onComplete(null);
                                                }
                                            } else {
                                                Log.w(TAG, "addOnCompleteListener: task1 is null");
                                                listener.onComplete(null);
                                            }
                                        });
                            } else {
                                Log.w(TAG, "FirebaseUser is null");
                                listener.onComplete(null);
                            }
                        } else {
                            Log.w(TAG, "Error creating user: " + task.getException());
                            listener.onComplete(null);
                        }
                    } else {
                        Log.w(TAG, "addOnCompleteListener: task is null");
                        listener.onComplete(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user", e);
                    listener.onComplete(null);
                });
    }



    public void checkUserCredentials(String usernameOrEmail, String password, OnCompleteListener<Boolean> listener) {
        mAuth.signInWithEmailAndPassword(usernameOrEmail, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onComplete(Tasks.forResult(true));
                    } else {
                        Log.w(TAG, "Error signing in", task.getException());
                        listener.onComplete(Tasks.forResult(false));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error signing in", e);
                    listener.onComplete(Tasks.forResult(false));
                });
    }

    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public void insertBirdSighting(BirdSighting birdSighting, OnCompleteListener<DocumentReference> listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            Map<String, Object> sightingMap = new HashMap<>();
            sightingMap.put("user_id", userId);
            sightingMap.put("bird_name", birdSighting.getBirdName());
            sightingMap.put("sighting_time", birdSighting.getSightingTime());
            sightingMap.put("user_latitude", birdSighting.getUserLatitude());
            sightingMap.put("user_longitude", birdSighting.getUserLongitude());
            sightingMap.put("photo_path", birdSighting.getPhotoPath());

            db.collection("bird_sightings")
                    .add(sightingMap)
                    .addOnCompleteListener(listener);
        }
    }

    public void getUserSpecificSightings(String userId, OnCompleteListener<List<BirdSighting>> listener) {
        CollectionReference sightingsRef = db.collection("bird_sightings");
        sightingsRef.whereEqualTo("user_id", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BirdSighting> sightings = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BirdSighting sighting = document.toObject(BirdSighting.class);
                            sightings.add(sighting);
                        }
                        listener.onComplete(Tasks.forResult(sightings));
                    } else {
                        Log.w(TAG, "Error getting bird sightings", task.getException());
                        listener.onComplete(null);
                    }
                });
    }
}