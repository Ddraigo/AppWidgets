package com.example.regreen.myapplication.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.ModelData.Reward;
import com.example.regreen.myapplication.MyAdapter;
import com.example.regreen.myapplication.User.Reward.ListRewardActivity;
import com.example.regreen.myapplication.User.Reward.ViewRewardDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class RewardFragment extends Fragment {

    private RecyclerView recyclerViewEcoGifts, recyclerViewCoupons;
    private MyAdapter<Reward> ecoGiftsAdapter, couponsAdapter;
    private FirebaseRepository<Reward> rewardRepository;
    private FirebaseImageHelper imageHelper;

    public RewardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reward, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerViews
        recyclerViewEcoGifts = view.findViewById(R.id.list_reward);
        recyclerViewCoupons = view.findViewById(R.id.list_reward2);

        recyclerViewEcoGifts.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCoupons.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Initialize Firebase helpers
        rewardRepository = new FirebaseRepository<>("Reward", Reward.class);
        imageHelper = new FirebaseImageHelper(requireContext());

        // Initialize adapters
        ecoGiftsAdapter = new MyAdapter<>(new ArrayList<>(), R.layout.item_reward, this::bindReward);
        couponsAdapter = new MyAdapter<>(new ArrayList<>(), R.layout.item_reward, this::bindReward);

        recyclerViewEcoGifts.setAdapter(ecoGiftsAdapter);
        recyclerViewCoupons.setAdapter(couponsAdapter);

        loadRewardsByCategory(1, ecoGiftsAdapter);
        loadRewardsByCategory(2, couponsAdapter);

        // Handle the "See All" button clicks for Eco Gifts and Coupons
        TextView tvSeeAllCate1 = view.findViewById(R.id.tvSeeAllCate1);
        TextView tvSeeAllCate2 = view.findViewById(R.id.tvSeeAllCate2);

        tvSeeAllCate1.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ListRewardActivity.class);
            intent.putExtra("CATEGORY_ID", 1);  // Category ID for Eco Gifts
            startActivity(intent);
        });

        tvSeeAllCate2.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ListRewardActivity.class);
            intent.putExtra("CATEGORY_ID", 2);  // Category ID for Coupons
            startActivity(intent);
        });
    }

    private void bindReward(View itemView, Reward reward) {
        TextView title = itemView.findViewById(R.id.rewardTitle);
        TextView points = itemView.findViewById(R.id.rewardPoint);
        ImageView image = itemView.findViewById(R.id.imgReward);

        title.setText(reward.getTitle());
        points.setText(String.format("%d Điểm", reward.getPoint()));

        if (reward.getImageResource() != null && !reward.getImageResource().isEmpty()) {
            imageHelper.loadImageFromRealtimeDatabase("Reward/" + reward.getIdReward() + "/imageResource", image);
        } else {
            image.setImageResource(R.drawable.eco_friendly_gifts_l); // Default placeholder
        }

        // Set click listener for item
        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ViewRewardDetailActivity.class);
            intent.putExtra(ViewRewardDetailActivity.EXTRA_REWARD, reward);  // Pass the Reward object
            startActivity(intent);
        });
    }

    private void loadRewardsByCategory(int categoryId, MyAdapter<Reward> adapter) {
        rewardRepository.getItemsWithFilter("category", String.valueOf(categoryId), new FirebaseRepository.OnFetchListListener<Reward>() {
            @Override
            public void onSuccess(List<Reward> data) {
                adapter.setData(data);
            }

            @Override
            public void onFailure(String message) {
                Log.e("RewardFragment", "Error loading category " + categoryId + ": " + message);
                Toast.makeText(getContext(), "Error loading category " + categoryId + ": " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
