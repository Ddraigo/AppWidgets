package com.example.regreen.myapplication.Admin.Reward;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.Reward;
import com.example.regreen.myapplication.MyAdapter;

import java.util.List;

public class RewardManagerActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRewardList;
    private MyAdapter<Reward> adapter;
    private TextView tv_empty_reward;
    private ImageButton btnBackManageReward;
    private Button btnAddReward;
    private FirebaseImageHelper imageHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_manager);

        getWindow().setStatusBarColor(getResources().getColor(R.color.green_bg, null));

        recyclerViewRewardList = findViewById(R.id.recyclerViewRewardList);
        recyclerViewRewardList.setLayoutManager(new LinearLayoutManager(this));
        tv_empty_reward = findViewById(R.id.tv_empty_reward);
        btnBackManageReward = findViewById(R.id.btnBackManageReward);
        btnAddReward = findViewById(R.id.btnAddReward);

        imageHelper = new FirebaseImageHelper(this);

        btnAddReward.setOnClickListener(v -> openAddRewardFragment());
        btnBackManageReward.setOnClickListener(v -> finish());

        loadRewards();

        getSupportFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
            if (result.getBoolean("dataUpdated")) {
                loadRewards();
            }
        });

        getSupportFragmentManager().setFragmentResultListener("rewardUpdated", this, (requestKey, result) -> loadRewards());
        getSupportFragmentManager().setFragmentResultListener("rewardDeleted", this, (requestKey, result) -> loadRewards());
    }

    private void openAddRewardFragment() {
        AddRewardFragment addRewardFragment = AddRewardFragment.rewardInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, addRewardFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadRewards() {
        FirebaseRepository<Reward> rewardRepository = new FirebaseRepository<>("Reward", Reward.class);

        rewardRepository.getAllRealTime(new FirebaseRepository.OnFetchListListener<Reward>() {
            @Override
            public void onSuccess(List<Reward> data) {
                Log.d("RewardManagerActivity", "Dữ liệu đã được cập nhật. Tổng số: " + data.size());
                if (data.isEmpty()) {
                    tv_empty_reward.setVisibility(View.VISIBLE);
                    recyclerViewRewardList.setVisibility(View.GONE);
                } else {
                    tv_empty_reward.setVisibility(View.GONE);
                    recyclerViewRewardList.setVisibility(View.VISIBLE);
                    setupRecyclerView(data);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("RewardManagerActivity", "Lỗi khi tải dữ liệu: " + message);
                Toast.makeText(RewardManagerActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
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
                    ImageView imgItem = itemView.findViewById(R.id.imgItem);

                    titleTextView.setText("Tiêu đề: " + reward.getTitle());
                    contentTextView.setText("Điểm: " + reward.getPoint());
                    dateTextView.setText("Ngày đăng: " + reward.getDateUp());

                    if (reward.getImageResource() != null && !reward.getImageResource().isEmpty()) {
                        displayBase64Image(reward.getImageResource(), imgItem);
                    } else {
                        imgItem.setImageResource(R.drawable.img_news_rectangle1); // Ảnh mặc định
                    }

                    itemView.setOnClickListener(v -> openRewardDetailFragment(reward));
                }
            });
            recyclerViewRewardList.setAdapter(adapter);
        } else {
            adapter.updateData(rewardList);
        }
    }

    private void displayBase64Image(String base64Image, ImageView imageView) {
        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(bitmap);
            } catch (IllegalArgumentException e) {
                Log.e("RewardManagerActivity", "Lỗi giải mã Base64: " + e.getMessage());
                imageView.setImageResource(R.drawable.img_news_rectangle1);
            }
        } else {
            imageView.setImageResource(R.drawable.img_news_rectangle1);
        }
    }

    private void openRewardDetailFragment(Reward reward) {
        RewardDetailFragment rewardDetailFragment = RewardDetailFragment.newInstance(
                reward.getIdReward(),
                reward.getPoint(),
                reward.getCategory(),
                reward.getTitle(),
                reward.getCondition(),
                reward.getImageResource(),
                reward.getDateUp()
        );

        rewardDetailFragment.show(getSupportFragmentManager(), "RewardDetailFragment");
    }
}
