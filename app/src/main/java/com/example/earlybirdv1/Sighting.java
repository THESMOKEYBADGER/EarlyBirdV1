package com.example.earlybirdv1;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Sighting extends Fragment {

    private TextView locationTextView;

    public Sighting() {
        // Required empty public constructor
    }

    public static Sighting newInstance(String birdName, String dateTime, double latitude, double longitude) {
        Sighting fragment = new Sighting();
        Bundle args = new Bundle();
        args.putString("birdName", birdName);
        args.putString("dateTime", dateTime);
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sighting, container, false);

        locationTextView = view.findViewById(R.id.locationTextView);

        String birdName = getArguments().getString("birdName");
        String dateTime = getArguments().getString("dateTime");
        double latitude = getArguments().getDouble("latitude");
        double longitude = getArguments().getDouble("longitude");

        updateLocationText(latitude, longitude);

        // Set the bird name and date-time in TextViews
        TextView birdNameTextView = view.findViewById(R.id.birdNameTextView);
        TextView dateTimeTextView = view.findViewById(R.id.dateTimeTextView);

        birdNameTextView.setText(birdName);
        dateTimeTextView.setText(dateTime);

        return view;
    }

    private void updateLocationText(double latitude, double longitude) {
        // Use a Geocoder to get the address from latitude and longitude
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                // Update the locationTextView with the address
                new Handler(Looper.getMainLooper()).post(() -> {
                    locationTextView.setText(address);
                });
            } else {
                locationTextView.setText("Location not found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationTextView.setText("Location not found");
        }
    }
}
