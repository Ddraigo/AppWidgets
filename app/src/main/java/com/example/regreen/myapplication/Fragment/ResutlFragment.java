package com.example.regreen.myapplication.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.regreen.R;
import com.example.regreen.myapplication.LearnMore;
import com.example.regreen.myapplication.RecivingPoint;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ResutlFragment extends BottomSheetDialogFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";


    private String mParam1;
    private String mParam2;
    private String mParam3;

    public ResutlFragment() {

    }


    public static ResutlFragment newInstance(String param1, String param2,String param3) {
        ResutlFragment fragment = new ResutlFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3,param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_resutl, container, false);

        String detectedClass = getArguments().getString(ARG_PARAM1);
        String confidence = getArguments().getString(ARG_PARAM2);
        String isrecycle = getArguments().getString(ARG_PARAM3);

        TextView tvResult = view.findViewById(R.id.tvResult);
        TextView tvConfidence = view.findViewById(R.id.tvConfidence);
        ImageView imgIsRecycle = view.findViewById(R.id.imgIsRecycle);
        TextView tvIsRecycle = view.findViewById(R.id.tvIsRecycle);

        tvResult.setText("Loại rác: " + detectedClass);
        tvConfidence.setText("Độ tin cậy: " + confidence);

        if (isrecycle.equals("1")) {
            imgIsRecycle.setImageResource(R.drawable.img_is_recycle);
            tvIsRecycle.setText("Rác tái chế");
            tvIsRecycle.setTextColor(getResources().getColor(R.color.green_btn));
        } else {
            imgIsRecycle.setImageResource(R.drawable.img_non_recycle);
            tvIsRecycle.setText("Không tái chế");
            tvIsRecycle.setTextColor(getResources().getColor(R.color.status_offline));
        }

        Button btnGotIt = view.findViewById(R.id.btnGotIt);
        btnGotIt.setOnClickListener(v -> dismiss());

        Button btnLearnMore = view.findViewById(R.id.btnLearnMore);
        btnLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LearnMore.class);
                startActivity(intent);
                dismiss();
            }
        });

        return view;
    }

}