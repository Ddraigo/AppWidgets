package com.example.regreen.myapplication;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.regreen.R;

public class HowToRecycleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_how_to_recycle);

        ImageButton btnBackHowToRecycle = findViewById(R.id.btnBackHowToRecycle);
        btnBackHowToRecycle.setOnClickListener(v -> finish());
    }
}