package com.example.earlybirdv1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class SightingAdapter extends RecyclerView.Adapter<SightingAdapter.SightingViewHolder> {
    private Context context;
    private List<BirdSighting> sightings;
    private OnItemClickListener onItemClickListener;
    private BirdSighting selectedSighting; // Add a member variable to store the selected sighting

    public interface OnItemClickListener {
        void onItemClick(BirdSighting sighting);
        void onImagePreviewClick(String imageUrl);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public SightingAdapter(Context context, List<BirdSighting> sightings) {
        this.context = context;
        this.sightings = sightings;
    }

    @NonNull
    @Override
    public SightingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_sighting, parent, false);
        return new SightingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SightingViewHolder holder, int position) {
        BirdSighting sighting = sightings.get(position);
        holder.birdNameTextView.setText(sighting.getBirdName());
        holder.dateTimeTextView.setText(sighting.getSightingTime());
        holder.nearestRoadTextView.setText(sighting.getNearestRoadName()); // Set nearest road name

        // Load image using Glide
        Glide.with(context)
                .load(sighting.getPhotoPath())
                .placeholder(R.drawable.placeholder_image) // Use a placeholder image if needed
                .into(holder.imagePreviewImageView);

        // Set click listeners
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(sighting);
                    selectedSighting = sighting; // Update the selected sighting
                }
            }
        });

        holder.imagePreviewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onImagePreviewClick(sighting.getPhotoPath());
                    selectedSighting = sighting; // Update the selected sighting
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return sightings.size();
    }

    public BirdSighting getSelectedSighting() {
        return selectedSighting;
    }

    static class SightingViewHolder extends RecyclerView.ViewHolder {
        TextView birdNameTextView;
        TextView dateTimeTextView;
        TextView nearestRoadTextView; // Add TextView for nearest road
        ImageButton imagePreviewImageView;

        SightingViewHolder(View itemView) {
            super(itemView);
            birdNameTextView = itemView.findViewById(R.id.birdNameTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            nearestRoadTextView = itemView.findViewById(R.id.locationTextView); // Use the correct ID
            imagePreviewImageView = itemView.findViewById(R.id.imagePreviewImageView);
        }
    }
}

