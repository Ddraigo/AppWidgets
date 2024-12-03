package com.example.regreen.myapplication.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.AccessPoint.AccessPointManagerActivity;
import com.example.regreen.myapplication.Admin.Booking.BookingManagerActivity;
import com.example.regreen.myapplication.Admin.New.NewManagerActivity;
import com.example.regreen.myapplication.Admin.Recycle.RecycleManagerActivity;
import com.example.regreen.myapplication.Admin.Reward.RewardManagerActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminHomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AdminHomeFragment() {}

    public static AdminHomeFragment newInstance(String param1, String param2) {
        AdminHomeFragment fragment = new AdminHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        // Find cardInputInfo and set an OnClickListener
        LinearLayout cardMangageCategory = view.findViewById(R.id.cardMangageCategory);
        LinearLayout cardManageNews = view.findViewById(R.id.cardManageNews);
        LinearLayout cardManageReward = view.findViewById(R.id.cardManageReward);
        LinearLayout cardManageUseBooking = view.findViewById(R.id.cardManageUseBooking);
        LinearLayout cardManageAccessPoint = view.findViewById(R.id.cardManageAccessPoint);

        cardMangageCategory.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RecycleManagerActivity.class);
            startActivity(intent);
        });

        cardManageNews.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), NewManagerActivity.class);
            startActivity(intent);
        });

        cardManageReward.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), RewardManagerActivity.class);
            startActivity(intent);
        });

        cardManageAccessPoint.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), AccessPointManagerActivity.class);
            startActivity(intent);
        });

        cardManageUseBooking.setOnClickListener(v ->{
            Intent intent = new Intent(getActivity(), BookingManagerActivity.class);
            startActivity(intent);
        });
        return view;
    }
}