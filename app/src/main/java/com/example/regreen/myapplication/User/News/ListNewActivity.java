package com.example.regreen.myapplication.User.News;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.ModelData.News;
import com.example.regreen.myapplication.MyAdapter;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;

import java.util.List;

public class ListNewActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNewList;
    private MyAdapter<News> adapter;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_new);

        recyclerViewNewList = findViewById(R.id.rvUserNewList);
        progressBar = findViewById(R.id.progressBar);
        recyclerViewNewList.setLayoutManager(new LinearLayoutManager(this));
        ImageButton btnBackUserListNew = findViewById(R.id.btnBackUserListNew);
        btnBackUserListNew.setOnClickListener(v -> finish());

        loadNewsData();
    }

    private void loadNewsData() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseRepository<News> newsRepository = new FirebaseRepository<>("News", News.class);
        newsRepository.getAllRealTime(new FirebaseRepository.OnFetchListListener<News>() {
            @Override
            public void onSuccess(List<News> data) {
                progressBar.setVisibility(View.GONE);

                if (data.isEmpty()) {
                    Toast.makeText(ListNewActivity.this, "No news available", Toast.LENGTH_SHORT).show();
                } else {
                    setupRecyclerView(data);
                }
            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListNewActivity.this, "Error loading data: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView(List<News> newsList) {
        if (adapter == null) {
            adapter = new MyAdapter<>(newsList, R.layout.add_item_layout, new MyAdapter.Binder<News>() {
                @Override
                public void bind(View itemView, News news) {
                    TextView titleTextView = itemView.findViewById(R.id.titleItem);
                    TextView contentTextView = itemView.findViewById(R.id.idItem);
                    TextView dateTextView = itemView.findViewById(R.id.dayAddItem);
                    ImageView imageView = itemView.findViewById(R.id.imgItem);

                    titleTextView.setText(news.getTitle());
                    contentTextView.setText(news.getContent());
                    dateTextView.setText(news.getDateUp());

                    if (news.getImageResource() != null && !news.getImageResource().isEmpty()) {
                        displayBase64Image(news.getImageResource(), imageView);
                    }

                    itemView.setOnClickListener(v -> openNewsDetailActivity(news));
                }
            });
            recyclerViewNewList.setAdapter(adapter);
        } else {
            adapter.updateData(newsList);
        }
    }

    private void openNewsDetailActivity(News news) {
        Intent intent = new Intent(ListNewActivity.this, ViewNewDetailActivity.class);
        intent.putExtra(ViewNewDetailActivity.EXTRA_NEWS, news);
        startActivity(intent);
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("ListNewActivity", "Error decoding Base64 image: " + e.getMessage());
            }
        }
    }

}
