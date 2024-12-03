package com.example.regreen.myapplication.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.regreen.R;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;

public class ActivityFragment extends Fragment {

    private ImageView imageView;
    private TabLayout tabLayout;

    public ActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        imageView = view.findViewById(R.id.imageView3);
        tabLayout = view.findViewById(R.id.tabActivity);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Tab 0: Sắp tới
                    imageView.setImageResource(R.drawable.img_sorting_waste); // upcoming_image
                } else if (tab.getPosition() == 1) {
                    // Tab 1: Lịch sử
                    imageView.setImageResource(R.drawable.img_is_recycle); // history_image
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Select the first tab by default
        tabLayout.getTabAt(0).select();

        return view;
    }
}
