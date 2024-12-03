package com.example.regreen.myapplication.User.Reward;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.News;
import com.example.regreen.myapplication.ModelData.Reward;
import com.example.regreen.myapplication.MyAdapter;
import com.example.regreen.myapplication.User.News.ListNewActivity;
import com.example.regreen.myapplication.User.News.ViewNewDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ListRewardActivity extends AppCompatActivity {

    private RecyclerView rvUserRewardList;
    private MyAdapter<Reward> adapter;
    private ProgressBar progressBar;
    private int categoryId;  // Store the category ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_reward);

        categoryId = getIntent().getIntExtra("CATEGORY_ID", 1);

        rvUserRewardList = findViewById(R.id.rvUserRewardList);
        progressBar = findViewById(R.id.progressBar);
        rvUserRewardList.setLayoutManager(new LinearLayoutManager(this));
        ImageButton btnBackUserListReward = findViewById(R.id.btnBackUserListReward);
        btnBackUserListReward.setOnClickListener(v -> finish());

        loadRewardsByCategory();
    }

    private void loadRewardsByCategory() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseRepository<Reward> rewardRepository = new FirebaseRepository<>("Reward", Reward.class);

        rewardRepository.getItemsWithFilter("category", String.valueOf(categoryId), new FirebaseRepository.OnFetchListListener<Reward>() {
            @Override
            public void onSuccess(List<Reward> data) {
                progressBar.setVisibility(View.GONE);

                if (data.isEmpty()) {
                    Toast.makeText(ListRewardActivity.this, "No rewards available for this category", Toast.LENGTH_SHORT).show();
                } else {
                    setupRecyclerView(data);
                }
            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ListRewardActivity.this, "Error loading data: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView(List<Reward> rewardList) {
        if (adapter == null) {
            adapter = new MyAdapter<>(rewardList, R.layout.add_item_layout, new MyAdapter.Binder<Reward>() {
                @Override
                public void bind(View itemView, Reward reward) {
                    TextView titleTextView = itemView.findViewById(R.id.titleItem);
                    TextView contentTextView = itemView.findViewById(R.id.idItem);
                    TextView dateTextView = itemView.findViewById(R.id.dayAddItem);
                    ImageView imageView = itemView.findViewById(R.id.imgItem);

                    titleTextView.setText(reward.getTitle());
                    contentTextView.setText(String.valueOf(reward.getPoint()));
                    dateTextView.setText(reward.getDateUp());

                    if (reward.getImageResource() != null && !reward.getImageResource().isEmpty()) {
                        displayBase64Image(reward.getImageResource(), imageView);
                    }

                    itemView.setOnClickListener(v -> openRewardDetailActivity(reward));
                }
            });
            rvUserRewardList.setAdapter(adapter);
        } else {
            adapter.updateData(rewardList);
        }
    }

    private void openRewardDetailActivity(Reward reward) {
        Intent intent = new Intent(ListRewardActivity.this, ViewRewardDetailActivity.class);
        intent.putExtra(ViewRewardDetailActivity.EXTRA_REWARD, reward);
        startActivity(intent);
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("ListRewardActivity", "Error decoding Base64 image: " + e.getMessage());
            }
        }
    }
}
