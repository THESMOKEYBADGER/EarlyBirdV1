package com.example.earlybirdv1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ImagePreviewBottomSheetFragment extends BottomSheetDialogFragment {
    private String imageUrl;

    public ImagePreviewBottomSheetFragment(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_preview_bottom_sheet, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        Glide.with(requireContext()).load(imageUrl).into(imageView);

        return view;
    }
}
