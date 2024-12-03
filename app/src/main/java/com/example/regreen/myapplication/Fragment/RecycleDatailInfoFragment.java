package com.example.regreen.myapplication.Fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.regreen.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class RecycleDatailInfoFragment extends BottomSheetDialogFragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_IMAGE = "image";
    private static final String ARG_DETAIL = "detail";

    private String itemName;
    private String itemImage;
    private String itemDetail;

    public RecycleDatailInfoFragment() {
        // Required empty public constructor
    }

    public static RecycleDatailInfoFragment newInstance(String name, String image, String detail) {
        RecycleDatailInfoFragment fragment = new RecycleDatailInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_IMAGE, image);
        args.putString(ARG_DETAIL, detail);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            itemName = getArguments().getString(ARG_NAME);
            itemImage = getArguments().getString(ARG_IMAGE);
            itemDetail = getArguments().getString(ARG_DETAIL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycle_datail_info, container, false);

        TextView tvNameRecycle = view.findViewById(R.id.tvNameRecycle);
        ImageView imgRecycle = view.findViewById(R.id.imgRecycle);
        TextView tvDetailRecycle = view.findViewById(R.id.tvDetailRecycle);

        tvNameRecycle.setText("Tên vật phẩm: " + itemName);

        tvDetailRecycle.setText(itemDetail);

        if (itemImage != null && !itemImage.isEmpty()) {
            byte[] decodedBytes = Base64.decode(itemImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            imgRecycle.setImageBitmap(bitmap);
        }

        return view;
    }
}
