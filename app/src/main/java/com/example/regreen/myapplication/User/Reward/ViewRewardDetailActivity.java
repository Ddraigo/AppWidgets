package com.example.regreen.myapplication.User.Reward;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.regreen.R;
import com.example.regreen.myapplication.ModelData.Reward;
import com.example.regreen.myapplication.User.News.ViewNewDetailActivity;

public class ViewRewardDetailActivity extends AppCompatActivity {

    public static final String EXTRA_REWARD = "reward";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reward_detail);

        Reward reward = (Reward) getIntent().getSerializableExtra(EXTRA_REWARD);

        ImageButton backButton = findViewById(R.id.btnBackUserRewardDetail);
        backButton.setOnClickListener(v -> finish());

        if (reward != null) {
            TextView titleTextView = findViewById(R.id.tv_ViewTitle2);
            TextView dateTextView = findViewById(R.id.rewardDate);
            TextView contentTextView = findViewById(R.id.rewardContent);
            TextView pointTextView = findViewById(R.id.rewardPoints);

            ImageView imageView = findViewById(R.id.rewardImage);

            titleTextView.setText(reward.getTitle());
            dateTextView.setText(reward.getDateUp());
            contentTextView.setText(reward.getCondition());
            pointTextView.setText(String.valueOf(reward.getPoint()));

            if (reward.getImageResource() != null && !reward.getImageResource().isEmpty()) {
                displayBase64Image(reward.getImageResource(), imageView);
            }
        }
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("ViewNewDetailActivity", "Error decoding Base64 image: " + e.getMessage());
            }
        }
    }

    public static void startActivity(AppCompatActivity activity, Reward reward) {
        Intent intent = new Intent(activity, ViewRewardDetailActivity.class);
        intent.putExtra(EXTRA_REWARD, reward);
        activity.startActivity(intent);
    }
}