package com.example.earlybirdv1;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class FetchBirdData {
    private RequestQueue requestQueue;
    private final String apiKey = "c30dctbmj6qg"; // Replace with your actual API key

    public FetchBirdData(Context context) {
        // Initialize the RequestQueue in the constructor
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public void fetchBirdData(double lat, double lng, int dist, final BirdDataListener listener) {
        // Update the URL with the new latitude, longitude, and distance
        String url = "https://api.ebird.org/v2/ref/hotspot/geo?lat=" + lat + "&lng=" + lng + "&dist=" + dist;

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Handle the API response here as a string
                        Log.d("API Response", response); // Log the response data

                        // Parse the response and provide the data to the listener
                        parseBirdData(response, listener);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors here
                        Log.e("API Error", "API request failed: " + error.getMessage());

                        // Pass the error to the listener
                        listener.onError(error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-ebirdapitoken", apiKey);
                return headers;
            }
        };

        Log.d("MyApp", "API request sent: " + url); // Log the updated URL
        requestQueue.add(stringRequest);
    }


    // Helper method to parse the bird data from the response string
    private void parseBirdData(String response, BirdDataListener listener) {
        // Split the response into lines
        String[] lines = response.split("\n");

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 6) {
                double latitude = Double.parseDouble(parts[4]);
                double longitude = Double.parseDouble(parts[5]);
                String name = parts[6];
                listener.onSuccess(latitude, longitude, name);
            }
        }
    }

    // Listener interface to handle bird data
    public interface BirdDataListener {
        void onSuccess(double latitude, double longitude, String name);
        void onError(VolleyError error);
    }
}
