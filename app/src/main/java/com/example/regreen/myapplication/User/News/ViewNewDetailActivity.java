package com.example.regreen.myapplication.User.News;

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
import com.example.regreen.myapplication.ModelData.News;

public class ViewNewDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NEWS = "news";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_new_detail);

        News news = (News) getIntent().getSerializableExtra(EXTRA_NEWS);

        ImageButton backButton = findViewById(R.id.btnBackUserNewDetail);
        backButton.setOnClickListener(v -> finish());

        if (news != null) {
            TextView titleTextView = findViewById(R.id.tv_ViewTitle);
            TextView dateTextView = findViewById(R.id.tv_ViewDateUp);
            TextView contentTextView = findViewById(R.id.tv_ViewContent);
            ImageView imageView = findViewById(R.id.tv_ViewImage);

            titleTextView.setText(news.getTitle());
            dateTextView.setText(news.getDateUp());
            contentTextView.setText(news.getContent());

            if (news.getImageResource() != null && !news.getImageResource().isEmpty()) {
                displayBase64Image(news.getImageResource(), imageView);
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

    // Static method to start the activity and pass the News object
    public static void startActivity(AppCompatActivity activity, News news) {
        Intent intent = new Intent(activity, ViewNewDetailActivity.class);
        intent.putExtra(EXTRA_NEWS, news);
        activity.startActivity(intent);
    }
}
