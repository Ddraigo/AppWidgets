package com.example.regreen.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.view.WindowInsetsController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;


import com.example.regreen.R;
import com.example.regreen.myapplication.Fragment.ActivityFragment;
import com.example.regreen.myapplication.Fragment.AdminHomeFragment;
import com.example.regreen.myapplication.Fragment.HomeFragment;
import com.example.regreen.myapplication.Fragment.ProfileFragment;
import com.example.regreen.myapplication.Fragment.RewardFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String userEmail = getIntent().getStringExtra("userEmail");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(R.color.green_bg, null));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        FloatingActionButton fab = findViewById(R.id.fab);

        bottomNavigationView.setOnItemSelectedListener(navListener);

        // Load the initial fragment
        if (userEmail != null && userEmail.equals("admin@gmail.com")) {
            AdminHomeFragment adminHomeFragment = new AdminHomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userEmail", userEmail);
            adminHomeFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, adminHomeFragment)
                    .commit();
        }
        else if (savedInstanceState == null) {
            HomeFragment homeFragment = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userEmail", userEmail);
            homeFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, homeFragment)
                    .commit();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WasteIdentification.class);
                startActivity(intent);
            }
        });
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;

        String userEmail = getIntent().getStringExtra("userEmail");

        switch (item.getItemId()) {

            case R.id.home:
                if(userEmail.equals("admin@gmail.com")){
                    AdminHomeFragment adminHomeFragment = new AdminHomeFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("userEmail", userEmail);
                    adminHomeFragment.setArguments(bundle);
                    selectedFragment = adminHomeFragment;
                }
                else {
                    HomeFragment homeFragment = new HomeFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("userEmail", userEmail);
                    homeFragment.setArguments(bundle);
                    selectedFragment = homeFragment;
                }


                break;
            case R.id.activity:
                selectedFragment = new ActivityFragment();
                break;
            case R.id.reward:
                selectedFragment = new RewardFragment();
                break;
            case R.id.profile:
                ProfileFragment profileFragment = new ProfileFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userEmail", userEmail);
                profileFragment.setArguments(bundle);
                selectedFragment = profileFragment;
                break;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, selectedFragment)
                    .commit();
        }
        return true;
    };
}

